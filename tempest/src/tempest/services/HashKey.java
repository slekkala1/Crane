package tempest.services;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by swapnalekkala on 11/2/15.
 */
public class HashKey {

    public static String hashKey(String key) {
        StringBuffer sb = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(key.getBytes());

            byte byteData[] = md.digest();

            //convert the byte to hex format method 1
            sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

            //System.out.println("Hex format : " + sb.toString());
        } catch (NoSuchAlgorithmException e) {
//
        }
        return sb.toString();
    }


    public static int hexToKey(String hex) {
        String bin = new BigInteger(hex, 16).toString(2);
        int m = 7;
        String bitsAfterTrucating = bin.substring(bin.length() - m);
        int foo = Integer.parseInt(bitsAfterTrucating, 2);
        return foo;
    }

//    public static void main(String[] args) {
//        HashKey hashKey = new HashKey();
//        System.out.println(hashKey.hexToBinary(hashKey.hashKey("fa15-cs425-g03-01.cs.illinois.edu:4444")));
//        System.out.println(hashKey.hexToBinary(hashKey.hashKey("fa15-cs425-g03-02.cs.illinois.edu:4444")));
//        System.out.println(hashKey.hexToBinary(hashKey.hashKey("fa15-cs425-g03-03.cs.illinois.edu:4444")));
//        System.out.println(hashKey.hexToBinary(hashKey.hashKey("fa15-cs425-g03-04.cs.illinois.edu:4444")));
//        System.out.println(hashKey.hexToBinary(hashKey.hashKey("fa15-cs425-g03-05.cs.illinois.edu:4444")));
//        System.out.println(hashKey.hexToBinary(hashKey.hashKey("fa15-cs425-g03-06.cs.illinois.edu:4444")));
//        System.out.println(hashKey.hexToBinary(hashKey.hashKey("fa15-cs425-g03-07.cs.illinois.edu:4444")));
////        Hex format : a6a60b3bee6b322042947ef3159ff97c717aeafe
//        126
//        Hex format : bb0ab537430bb27d25de2c6e5b58556f64c210cc
//        76
//        Hex format : 35655f68a0297f819897ea8bed3f6b52bb47be74
//        116
//        Hex format : 41d49da0ac7708766de1fdc4abb098ede32b424d
//        77
//        Hex format : 343c906bf0a3bf67e3b97e2a54611eefbed17cbf
//        63
//        Hex format : f98138f5028c84d303488897f5fcaeb6470ec0fc
//        124
//        Hex format : c8a7e634493c6670828d237dd880a03ef9c5c6b2
//        50
//    }


}