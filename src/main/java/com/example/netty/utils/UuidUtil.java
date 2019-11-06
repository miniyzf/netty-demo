package com.example.netty.utils;

import java.util.UUID;

public class UuidUtil {

	public static String get32UUID() {
		String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
		return uuid;
	}

	public static String get6NumberUUID(int number){
		int num[]={0,1,2,3,4,5,6,7,8,9};
		int length=num.length;
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<length;i++){
			int random= (int) (Math.random()*length);
			if(i<number){
				sb.append(num[random]);
			}else{
				break;
			}
		}
		return sb.toString();
	}
	public static String get24WordUUID(int number){ //不包含I O
		char num[]={'A','B','C','D','E','F','G','H','J','K','L','M','N','P','Q','R','S','T','U','V','W','X','Y','Z'};
		int length=num.length;
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<length;i++){
			int random= (int) (Math.random()*length);
			if(i<number){
				sb.append(num[random]);
			}else{
				break;
			}
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		System.out.println(get32UUID());
		System.out.println(get6NumberUUID(5));
		System.out.println(get24WordUUID(1));
	}
}

