package com.java.practice;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;


public class DecodePassword
{
    public static void main(String[] args)
    {
        String password = "";  
        		// Ec0l0g!c = "1826378097404620596037";
        		//guest = "8001581053287"; // This needs to be decrypted.
        BigInteger passwordAsInteger = new BigInteger(password);
        String decodedPassword = EncryptionService.decode(passwordAsInteger);
        System.out.println("decodedPassword : "+ decodedPassword);
      

    }
}

