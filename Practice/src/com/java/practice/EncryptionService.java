package com.java.practice;

import java.io.*;
import java.math.BigInteger;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      ECOlogic Systems Corporation
 * @author
 * @version 1.0
 */

public class EncryptionService
{
   // private static final Logger logger=LogManager.getLogger(EncryptionService.class);
    private static final String base64Alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_\"";
    private static final int numBitsPerValue = 28; // Must match the value for bs in crypto.js; must be <= 32.

    private EncryptionService()
    {
        //intentional
    }

    private static BigInteger convertFromArray(long array[])
    {
        BigInteger value = BigInteger.ZERO;
        int index = array.length;
        while (--index >= 0)
        {
            value = value.shiftLeft(32);
            value = value.add(BigInteger.valueOf(array[index]));
        }
        return value;
    }

    // The Javascript represents this value as little-endian
    private static BigInteger convertFromText(byte text[], int startIndex, int stopIndex)
    {
        BigInteger value = BigInteger.ZERO;
        int wordbitnum = 0;
        int bitcounter = 0;

        for (int index = startIndex; index <= stopIndex; index++)
        {
            byte character = text[index];
            for (int charbit = 0; charbit < 8; charbit++)
            {
                if ((character & (1 << charbit)) != 0)
                {
                    value = value.setBit(wordbitnum);
                }
                wordbitnum++;
                bitcounter++;
                if (bitcounter == numBitsPerValue)
                {
                    wordbitnum = (((wordbitnum - 1) / 32) + 1) * 32;
                    bitcounter = 0;
                }
            }
        }
        return value;
    }

    private static byte[] convertToText(BigInteger encodedString)
    {
        int bits = encodedString.bitLength();
        int numdblwords = ((bits - 1) / 32) + 1;
        ByteArrayOutputStream plainText = new ByteArrayOutputStream();
        byte decodedChar = 0;
        int charbit = 0;

        for (int index = 0; index < numdblwords; index++)
        {
            BigInteger dblword = encodedString.shiftRight(index * 32);
            for (int dblwordbit = 0; dblwordbit < numBitsPerValue; dblwordbit++)
            {
                if (dblword.testBit(dblwordbit))
                {
                    decodedChar |= (1 << charbit);
                }
                charbit++;
                if (charbit == 8)
                {
                    plainText.write(decodedChar);
                    decodedChar = 0;
                    charbit = 0;
                }
            }
        }
        return plainText.toByteArray();
    }

    public static BigInteger encode(String string)
    {
        return convertFromText(string.getBytes(), 0, string.length() - 1);
    }

    public static String decode(BigInteger encodedString)
    {
        byte rawText[] = convertToText(encodedString);
        int lastValidChar = rawText.length - 1;
        while (lastValidChar > 0 && rawText[lastValidChar] == 0)
        {
            lastValidChar--;
        }
        return new String(rawText, 0, lastValidChar + 1);
    }

    private static BigInteger packInteger(BigInteger unpacked)
    {
        BigInteger packed = BigInteger.ZERO;
        int counter = 0;
        int srcBit = 0;
        int destBit = 0;
        while (srcBit < unpacked.bitLength())
        {
            if (unpacked.testBit(srcBit))
            {
                packed = packed.setBit(destBit);
            }
            srcBit++;
            destBit++;
            counter++;
            if (counter == numBitsPerValue)
            {
                counter = 0;
                srcBit += 32 - numBitsPerValue;
            }
        }
        return packed;
    }

    private static BigInteger unpackInteger(BigInteger packed)
    {
        BigInteger unpacked = BigInteger.ZERO;
        int counter = 0;
        int srcBit = 0;
        int destBit = 0;
        while (srcBit < packed.bitLength())
        {
            if (packed.testBit(srcBit))
            {
                unpacked = unpacked.setBit(destBit);
            }
            srcBit++;
            destBit++;
            counter++;
            if (counter == numBitsPerValue)
            {
                counter = 0;
                destBit += 32 - numBitsPerValue;
            }
        }
        return unpacked;
    }

    private static byte[] convertFromBase64(String text)
    {
        // Convert text from Base64
        ByteArrayOutputStream plainText = new ByteArrayOutputStream();
        int shift = 0;
        int mask = 0;
        int charPos;

        for (int index = 0; index < text.length(); index++)
        {
            charPos = base64Alphabet.indexOf(text.charAt(index));
            if (charPos >= 0)
            {
                if (shift > 0)
                {
                    Integer bitpattern = new Integer(charPos << (8 - shift) & 255 | mask);
                    plainText.write(bitpattern.byteValue());
                }
                mask = charPos >> shift;
                shift += 2;
                if (shift == 8)
                {
                    shift = 0;
                }
            }
        }
        return plainText.toByteArray();
    }

    private static String rc4Decode(byte key[], byte text[], int startIndex)
    {
        byte charmap[] = new byte[256];
        for (int charcode = 0; charcode < 256; charcode++)
        {
            Integer converter = new Integer(charcode);
            charmap[charcode] = converter.byteValue();
        }

        byte swap;
        int index = key.length;
        int offset = 0;

        while (index-- > 0)
        {
            int charvalue = 0x000000FF & charmap[index];
            int keyvalue = 0x000000FF & key[index];
            offset += keyvalue;
            offset += charvalue;
            offset %= 256;
            swap = charmap[index];
            charmap[index] = charmap[offset];
            charmap[offset] = swap;
        }

        char plaintext[] = new char[text.length - startIndex];

        offset = 0;
        for (index = 0; index < (text.length - startIndex); index++)
        {
            int indexMask = index & 255;
            offset += (char)charmap[indexMask];
            offset &= 255;
            swap = charmap[indexMask];
            charmap[indexMask] = charmap[offset];
            charmap[offset] = swap;
            int charMaskIndex = (char)charmap[indexMask] + (char)charmap[offset];
            charMaskIndex %= 256;
            plaintext[index] = (char)(text[index + startIndex] ^ charmap[charMaskIndex]);
        }

        return new String(plaintext);
    }

    public static String rsaDecode(BigInteger privateExponent, BigInteger modulus, String text)
    {
        byte textBuf[] = convertFromBase64(text);
        int sessionKeyLength = textBuf[0];
        BigInteger sessionKeyValue = convertFromText(textBuf, 1, sessionKeyLength);
        sessionKeyValue = packInteger(sessionKeyValue);
        sessionKeyValue = sessionKeyValue.modPow(packInteger(privateExponent), packInteger(modulus));
        sessionKeyValue = unpackInteger(sessionKeyValue);
        byte sessionKey[] = convertToText(sessionKeyValue);
        return rc4Decode(sessionKey, textBuf, sessionKeyLength + 1);
    }

    public static String rsaDecode(String text)
    {
        long modulusArray[] = { 112638481,176343822,48504204,93159832,63359 };
        long privateExponentArray[] = { 95311723,44529229,212936231,252891716,56689 };
        BigInteger modulus = convertFromArray(modulusArray);
        BigInteger privateExponent = convertFromArray(privateExponentArray);
        return rsaDecode(privateExponent, modulus, text);
    }

}