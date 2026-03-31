package com.sp.app.security;

public class NumericRoleGranted {
	public final static int INACTIVE = 0; // 비회원
	public final static int USER = 1;	// 일반회원
	public final static int INSTRUCTOR = 31;	// 강사
	public final static int EX_EMP = 50;	// 퇴사사원
	public final static int EMP = 51;	// 사원
	public final static int ADMIN = 99;	// 관리자
	
	public static int getUserLevel(String authority) {
		try {
			switch (authority) {
			case "USER" : return USER;
			case "INSTRUCTOR" : return INSTRUCTOR;
			case "EX_EMP" : return EX_EMP;
			case "EMP" : return EMP;
			case "ADMIN" : return ADMIN;
			}
		} catch (Exception e) {
		}
		
		return 0;
	}
}
