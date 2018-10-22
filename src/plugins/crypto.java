package plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

import service.config;

public class crypto {
	public static AESUtil AesUtil = null;
	
	//init AESUtil
	static {
		AesUtil = new AESUtil(config.SecKeyPath);
	}
	
	public static String getUUID() {
		return UUID.randomUUID().toString().replace("-","");
	}
	
	public static String getHash(String str) {
		MessageDigest messageDigest;
		String enc = "";
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(str.getBytes("UTF-8"));
			enc = byte2Hex(messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return enc;
	}
	
	private static String byte2Hex(byte[] bytes){
		StringBuffer stringBuffer = new StringBuffer();
		String temp = null;
		for (int i=0;i<bytes.length;i++){
			temp = Integer.toHexString(bytes[i] & 0xFF);
			if (temp.length()==1){
				stringBuffer.append("0");
			}
			stringBuffer.append(temp);
		}
		return stringBuffer.toString();
	}
	
	public static void main(String args[]) {
		String str = "aaaa";
		String enc = crypto.AesUtil.enc(str);
		System.out.println(enc+":"+crypto.AesUtil.dec(enc));
	}
}

class AESUtil {
	private static final String KEY_ALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final String DEFAULT_SECKEY_FILE_PATH = "Sec_AES.key";
    private byte[] SecretKey = null;
    private SecretKeySpec SKS;
    
    AESUtil(){
    	this.loadSecretKey(DEFAULT_SECKEY_FILE_PATH);
    }
    
    AESUtil(byte[] SecretKey){
    	if(SecretKey!=null) {
    		this.SecretKey = SecretKey;
    		this.SKS = new SecretKeySpec(this.SecretKey, KEY_ALGORITHM);
    	}
    }
    
    AESUtil(String path){
    	this.loadSecretKey(path);
    }
    
    private byte[] getSecretKey() {
        KeyGenerator kg = null;
        try {
            kg = KeyGenerator.getInstance(KEY_ALGORITHM);
            kg.init(128, new SecureRandom());//生成128位密钥
            SecretKey secretKey = kg.generateKey();
            return secretKey.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void saveSecKey(String path) {
    	File file = new File(path);
    	OutputStream outputStream = null;
    	if (!file.exists()) {
    		try {
    			file.createNewFile();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    	try {
			outputStream = new FileOutputStream(file);
			outputStream.write(this.SecretKey);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public byte[] readSecKey(String path) {
    	File SecKeyFile = new File(path);
    	if (SecKeyFile.exists()) {
    		InputStream inpStream;
			byte[] SecretKey = null;
			try {
				inpStream = new FileInputStream(SecKeyFile);
				SecretKey = new byte[(int)SecKeyFile.length()]; 
				inpStream.read(SecretKey);
				inpStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return SecretKey;
    	}else {
    		return null;
    	}
    }
    
    public void loadSecretKey(String path) {
    	byte[] SecretKey = this.readSecKey(path);
    	if(SecretKey!=null) {
    		this.SecretKey = SecretKey;
    	}else{
    		this.SecretKey = this.getSecretKey();
			this.saveSecKey(path);
    	}
    	if(this.SecretKey != null) {
    		this.SKS = new SecretKeySpec(this.SecretKey, KEY_ALGORITHM);
    	}
    }
    
    public String enc(String str) {
    	Cipher cipher;
    	byte[] result = null;
		try {
			cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
			byte[] byteContent = str.getBytes("utf-8");
	        cipher.init(Cipher.ENCRYPT_MODE, this.SKS);
	        result = cipher.doFinal(byteContent);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
        return Base64.encodeBase64String(result);
    }
    
    public String dec(String encStr) {
    	Cipher cipher;
    	byte[] bytesResult = null;
    	String res = null;
		try {
			cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, this.SKS);
	        bytesResult = cipher.doFinal(Base64.decodeBase64(encStr));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			//主要是这里，密钥格式错误
			System.out.println("Wrong key");
			e.printStackTrace();
		}
		
		try {
			res = new String(bytesResult, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return res;
    }
}
