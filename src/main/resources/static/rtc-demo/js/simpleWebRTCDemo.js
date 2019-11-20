var userId = Math.random().toString(36).substr(2); // store local userId
var localStream; // local video stream object
var pc = null; // webrtc RTCPeerConnection
var socket = null;

var room = "123";

if(!window.WebSocket){
    console.log("您的浏览器不支持WebSocket协议！");
}else {
    socket = new WebSocket("ws://192.168.0.168:1234/wsPath?uid="+userId);

    socket.onopen =  function() {
        console.log("Signal server connected !");

        if (room !== '') {
            console.log('Attempted to join room:', userId, room);
            var args = {
                'userId': userId,
                'roomName': room
            };
            send(args);
        }
    };
    socket.onclose = function(event){
        console.log("Signal server closed !");
    };
    socket.onmessage = function(event) {
        var msg = JSON.parse(event.data).msg;
        console.log('Broadcast Received: ', msg);
        if (userId == msg.userId) {
            return;
        }
        console.log('Broadcast Received: ', msg.userId);
    };
}

function send(msg){
    if(!window.WebSocket){return;}
    if(socket.readyState == WebSocket.OPEN){
        socket.send(JSON.stringify({uid:userId,msg:msg}));
    }else{
        console.log("WebSocket 连接没有建立成功！");
    }
}


var webrtc = new SimpleWebRTC({
    // the id/element dom element that will hold "our" video
    localVideoEl: 'localVideo',
    // the id/element dom element that will hold remote videos
    remoteVideosEl: 'remoteVideos',
    // immediately ask for camera access
    autoRequestMedia: true,
    //media: {video: true, audio: true},
    //配置成自己的 signal 服务器
    url:'192.168.0.168',
    //文本聊天时，用户的昵称
    nick: 'btcx'
});


// we have to wait until it's ready
webrtc.on('readyToCall', function () {
    // you can name it anything
    webrtc.joinRoom(room);

    // Send a chat message
     /*$('#send').click(function () {
         var msg = $('#text').val();
         webrtc.sendToAll('chat', { message: msg, nick: webrtc.config.nick });
         $('#messages').append('<br>You:<br>' + msg + '\n');
         $('#text').val('');
     });*/
});

//For Text Chat ------------------------------------------------------------------
// Await messages from others
/*
webrtc.connection.on('message', function (data) {
    if (data.type === 'chat') {
        console.log('chat received', data);
        $('#messages').append('<br>' + data.payload.nick + ':<br>' + data.payload.message+ '\n');
    }
});*/
