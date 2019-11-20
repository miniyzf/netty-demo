const MESSAGE_TYPE_OFFER = 0x01;
const MESSAGE_TYPE_ANSWER = 0x02;
const MESSAGE_TYPE_CANDIDATE = 0x03;
const MESSAGE_TYPE_HANGUP = 0x04;

var localUserId = Math.random().toString(36).substr(2); // store local userId
var localStream; // local video stream object
var pc = null; // webrtc RTCPeerConnection
var socket = null;
/////////////////////////////////////////////
//弹出一个输入窗口
// var room = prompt('Enter room name:');
var room = "123";

if(!window.WebSocket){
    console.log("您的浏览器不支持WebSocket协议！");
}else {
    socket = new WebSocket("ws://localhost:8083/wsPath?uid="+localUserId);

    socket.onopen =  function() {
        console.log("Signal server connected !");

        if (room !== '') {
            console.log('Attempted to join room:', localUserId, room);
            var args = {
                'userId': localUserId,
                'roomName': room
            };
            send(args);
        }
    };
    socket.onclose = function(event){
        console.log("Signal server closed !")
    };
    socket.onmessage = function(event) {
        // {uid:uid,msg:msg,type:1}
        var message = JSON.parse(event.data);

        if(message.type == 1){
            document.getElementById("message").value += "【" + message.uid + "】 " + message.msg + "\r\n";
            return;
        }
        var msg = message.msg;


        console.log('Broadcast Received: ', msg);
        if (localUserId == msg.userId) {
            return;
        }
        console.log('Broadcast Received: ', msg.userId);
        switch (msg.msgType) {
            case MESSAGE_TYPE_OFFER:
                handleRemoteOffer(msg);
                break;
            case MESSAGE_TYPE_ANSWER:
                handleRemoteAnswer(msg);
                break;
            case MESSAGE_TYPE_CANDIDATE:
                handleRemoteCandidate(msg);
                break;
            case MESSAGE_TYPE_HANGUP:
                handleRemoteHangup();
                break;
            default:
                break;
        }
    };
}
function sendMsg(){ // 消息
    if(!window.WebSocket){return;}
    if(socket.readyState == WebSocket.OPEN){
        var msg = document.getElementById("text").value;
        if(msg.trim() != ""){
            socket.send(JSON.stringify({uid:localUserId,msg:msg,type:1}));
        }
    }else{
        console.log("WebSocket 连接没有建立成功！");
    }
}
function send(msg){ // 视频流
    if(!window.WebSocket){return;}
    if(socket.readyState == WebSocket.OPEN){
        socket.send(JSON.stringify({uid:localUserId,msg:msg,type:0}));
    }else{
        console.log("WebSocket 连接没有建立成功！");
    }
}


function handleRemoteOffer(msg) {
    console.log('Remote offer received: ', msg.sdp);
    if (pc == null) {
        createPeerConnection()
    }
    var sdp = new RTCSessionDescription({
        'type': 'offer',
        'sdp': msg.sdp
    });
    pc.setRemoteDescription(sdp);
    doAnswer();
}

function handleRemoteAnswer(msg) {
    console.log('Remote answer received: ', msg.sdp);
    var sdp = new RTCSessionDescription({
        'type': 'answer',
        'sdp': msg.sdp
    });
    pc.setRemoteDescription(sdp);
}

function handleRemoteCandidate(msg) {
    console.log('Remote candidate received: ', msg.candidate);
    var candidate = new RTCIceCandidate({
        sdpMLineIndex: msg.label,
        candidate: msg.candidate
    });
    pc.addIceCandidate(candidate);    
}

function handleRemoteHangup() {
    console.log('Remote hangup received');
    hangup();
}

////////////////////////////////////////////////////

var localVideo = document.querySelector('#localVideo');
var remoteVideo = document.querySelector('#remoteVideo');


//  -------------------------------------------------------------------------------------------------------------------->
/*
// Older browsers might not implement mediaDevices at all, so we set an empty object first
if (navigator.mediaDevices === undefined) {
    navigator.mediaDevices = {};
}

// Some browsers partially implement mediaDevices. We can't just assign an object
// with getUserMedia as it would overwrite existing properties.
// Here, we will just add the getUserMedia property if it's missing.
if (navigator.mediaDevices.getUserMedia === undefined) {
    navigator.mediaDevices.getUserMedia = function(constraints) {

        // First get ahold of the legacy getUserMedia, if present
        var getUserMedia = navigator.webkitGetUserMedia || navigator.mozGetUserMedia;

        // Some browsers just don't implement it - return a rejected promise with an error
        // to keep a consistent interface
        if (!getUserMedia) {
            return Promise.reject(new Error('getUserMedia is not implemented in this browser'));
        }

        // Otherwise, wrap the call to the old navigator.getUserMedia with a Promise
        return new Promise(function(resolve, reject) {
            getUserMedia.call(navigator, constraints, resolve, reject);
        });
    }
}
*/

function hasGetUserMedia(){
    return !!(navigator.mediaDevices && navigator.mediaDevices.getUserMedia);
}
if(hasGetUserMedia()){
    /*
    * 获取设备的媒体流（即 MediaStream）
    * 旧 api 接口: navigator.getUserMedia
    */
    /*navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia;
    navigator.getUserMedia({
        audio: true,
        video: true
    },openLocalStream,(e)=> {
        console.error("getUserMedia() error: " + e.name);
        alert('getUserMedia() error: ' + e.name);
    });*/
    /*
    * 获取设备的媒体流（即 MediaStream）
    * 新 api 接口: navigator.mediaDevices.getUserMedia
    * 本地测试OK
    * ip网址访问，需要 https 协议
    */
    navigator.mediaDevices.getUserMedia({
        audio: true,
        video: true
    }).then(openLocalStream)
        .catch(function(e) {
            console.error("getUserMedia() error: " + e.name);
            alert('getUserMedia() error: ' + e.name);
        });
}else {
    console.log('getUserMedia() is not supported by your browser');
}
//  <--------------------------------------------------------------------------------------------------------------------

function openLocalStream(stream) {
    console.log('Open local video stream');
    // 判断是否支持 srcObject 属性
    if ('srcObject' in localVideo) {
        localVideo.srcObject = stream;
    } else {
        localVideo.src = window.URL.createObjectURL(stream);
    }
    localStream = stream;
}

function createPeerConnection() {
    try {
        var PeerConnection = window.RTCPeerConnection || window.mozRTCPeerConnection || window.webkitRTCPeerConnection;
        pc = new PeerConnection(null);
        // pc = new RTCPeerConnection(null);
        pc.onicecandidate = handleIceCandidate;
        pc.onaddstream = handleRemoteStreamAdded;
        pc.onremovestream = handleRemoteStreamRemoved;
        pc.addStream(localStream);
        console.log('RTCPeerConnnection Created');
    } catch (e) {
        console.log('Failed to create PeerConnection, exception: ' + e.message);
        alert('Cannot create RTCPeerConnection object.');
        return;
    }
}

/////////////////////////////////////////////////////////

function doCall() {



    console.log('Starting call: Sending offer to remote peer');
    if (pc == null) {
        createPeerConnection()
    }
    pc.createOffer(createOfferAndSendMessage, handleCreateOfferError);

}

function doAnswer() {
    console.log('Answer call: Sending answer to remote peer');
    if (pc == null) {
        createPeerConnection()
    }
    pc.createAnswer().then(createAnswerAndSendMessage, handleCreateAnswerError);
}

function createOfferAndSendMessage(sessionDescription) {
    console.log('CreateOfferAndSendMessage sending message', sessionDescription);
    pc.setLocalDescription(sessionDescription);
    var message = {
        'userId': localUserId,
        'msgType': MESSAGE_TYPE_OFFER,
        'sdp': sessionDescription.sdp
    };
    send(message);
    console.log('Broadcast Offer:', message);
}

function createAnswerAndSendMessage(sessionDescription) {
    console.log('CreateAnswerAndSendMessage sending message', sessionDescription);
    pc.setLocalDescription(sessionDescription);
    var message = {
        'userId': localUserId,
        'msgType': MESSAGE_TYPE_ANSWER,
        'sdp': sessionDescription.sdp
    };
    send(message);
    console.log('Broadcast Answer:', message);
}

function handleCreateOfferError(event) {
    console.log('CreateOffer() error: ', event);
}

function handleCreateAnswerError(error) {
    console.log('CreateAnswer() error: ', error);
}

function handleIceCandidate(event) {
    console.log('Handle ICE candidate event: ', event);
    if (event.candidate) {
        var message = {
            'userId': localUserId,
            'msgType': MESSAGE_TYPE_CANDIDATE,
            'id': event.candidate.sdpMid,
            'label': event.candidate.sdpMLineIndex,
            'candidate': event.candidate.candidate
        };
        send(message);
        console.log('Broadcast Candidate:', message);
    } else {
        console.log('End of candidates.');
    }
}

function handleRemoteStreamAdded(event) {
    console.log('Handle remote stream added.');
    remoteVideo.srcObject = event.stream;
}

function handleRemoteStreamRemoved(event) {
    console.log('Handle remote stream removed. Event: ', event);
    remoteVideo.srcObject = null;
}

function hangup() {
    console.log('Hanging up !');
    remoteVideo.srcObject = null;
    if (pc != null) {
        pc.close();
        pc = null;
    }
}

/////////////////////////////////////////////////////////

document.getElementById('startCall').onclick = function() {
    console.log('Start call');
    doCall();
};

document.getElementById('endCall').onclick = function() {
    console.log('End call');
    hangup();
    var message = {
        'userId': localUserId,
        'msgType': MESSAGE_TYPE_HANGUP,
    };
    send(message);
    console.log('Broadcast Hangup:', message);
};


