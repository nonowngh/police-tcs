package mb.fw.tcs.modules.pics.common.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.gpki.gpkiapi_jni;
import com.gpki.gpkiapi.GpkiApi;
import com.gpki.gpkiapi.cert.X509Certificate;
import com.gpki.gpkiapi.crypto.PrivateKey;
import com.gpki.gpkiapi.storage.Disk;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GpkiUtil {
	byte[] myEnvCert, myEnvKey, mySigCert, mySigKey;
	private Map<String, X509Certificate> targetServerCertMap = new HashMap<String, X509Certificate>();
	
	public static String PREFIX_SIGNED_EXTENTION = ".Signed";
	public static String PREFIX_ENCRYPTED_EXTENTION = ".Gpki";

	// properties
	private String myServerId;
	private String targetServerIdList;
	private String envCertFilePathName;
	private String envPrivateKeyFilePathName;
	private String envPrivateKeyPasswd;
	private String sigCertFilePathName;
	private String sigPrivateKeyFilePathName;
	private String sigPrivateKeyPasswd;
	private String certFilePath;
	private String gpkiLicPath = ".";
	private boolean isLDAP;
	private boolean testGPKI = false;

	public void init(String ldapUrl) throws Exception {
		GpkiApi.init(gpkiLicPath);
		gpkiapi_jni gpki = this.getGPKI();
		if(log.isDebugEnabled()){
			if(gpki.API_GetInfo()==0)
				log.debug(gpki.sReturnString);
			else
				log.error(gpki.sDetailErrorString);
		}
		if(targetServerIdList!=null){
			String certIdList[] = targetServerIdList.split(",");
			for(int i = 0 ; i < certIdList.length ; i++){
				String certId = certIdList[i].trim();
				if(!certId.equals("")){
					load(gpki, certId, ldapUrl);
				}
			}
		}
		
		log.info("Loading gpki certificate : myServerId="+ this.getMyServerId());
		
		X509Certificate _myEnvCert = Disk.readCert(this.getEnvCertFilePathName());myEnvCert = _myEnvCert.getCert();
		PrivateKey _myEnvKey = Disk.readPriKey(this.getEnvPrivateKeyFilePathName(), this.getEnvPrivateKeyPasswd());	myEnvKey = _myEnvKey.getKey();
		X509Certificate _mySigCert = Disk.readCert(this.getSigCertFilePathName());mySigCert = _mySigCert.getCert();
		PrivateKey _mySigKey = Disk.readPriKey(this.getSigPrivateKeyFilePathName(), this.getSigPrivateKeyPasswd());	mySigKey = _mySigKey.getKey();
		
		//test my cert GPKI
		if(testGPKI){
			load(gpki, this.getMyServerId(), ldapUrl);
			testGpki(gpki);
		}
		this.finish(gpki);
		log.info("GpkiUtil initialized");
	}

	private void load(gpkiapi_jni gpki, String certId, String ldapUrl) throws Exception {

		log.debug("Loading gpki certificate : targetServerId="+ certId);

		X509Certificate cert = targetServerCertMap.get(certId);
		if (cert != null) {
			return;
		}

		if (isLDAP) {
//			String ldapUrl = "ldap://cen.dir.go.kr:389/cn=";
			String ldapUri;
			if (certId.charAt(3) > '9') { 
				ldapUri = ",ou=Group of Server,o=Public of Korea,c=KR";
			} else {
				ldapUri = ",ou=Group of Server,o=Government of Korea,c=KR";
			}
			
			int ret = gpki.LDAP_GetAnyDataByURL("userCertificate;binary", ldapUrl + "/cn=" + certId + ldapUri);
			this.checkResult(ret, gpki);
			cert = new X509Certificate(gpki.baReturnArray);
		} else {
			if(certFilePath != null){
				cert = Disk.readCert(certFilePath + File.separator + certId + ".cer");
			}else{
				log.debug("not certFilePath");
			}
		}

		targetServerCertMap.put(certId, cert);
	}
	
	private gpkiapi_jni getGPKI(){
		gpkiapi_jni gpki = new gpkiapi_jni();
		if(gpki.API_Init(gpkiLicPath) != 0){
			log.error(gpki.sDetailErrorString);
		}
		return gpki;
	}
	private void finish(gpkiapi_jni gpki){
		if(gpki.API_Finish() != 0){
			log.error(gpki.sDetailErrorString);
		}
	}

	public byte[] encrypt(byte[] plain, String certId , boolean load) throws Exception {
		X509Certificate targetEnvCert = targetServerCertMap.get(certId);
		if (targetEnvCert == null) {
			throw new Exception("Certificate not found : targetServerId=" + certId);
		}
		
		gpkiapi_jni gpki = this.getGPKI();
		try{
			int result = gpki.CMS_MakeEnvelopedData(targetEnvCert.getCert(), plain,
					gpkiapi_jni.SYM_ALG_NEAT_CBC);
			checkResult(result, "Fail to encrypt message", gpki);
	
			return gpki.baReturnArray;
		}catch(Exception ex){
			throw ex;
		}finally{
			finish(gpki);
		}
	}
	
	public byte[] encrypt(byte[] plain, String certId) throws Exception {
		return encrypt(plain,certId , false);
	}
	
	public byte[] decrypt(byte[] encrypted) throws Exception {

		gpkiapi_jni gpki = this.getGPKI();
		try{
			int result = gpki.CMS_ProcessEnvelopedData(myEnvCert, myEnvKey,
					encrypted);
			checkResult(result, "Fail to decrpyt message", gpki);
	
			return gpki.baReturnArray;
		}catch(Exception ex){
			throw ex;
		}finally{
			finish(gpki);
		}
	}

	public byte[] sign(byte[] plain) throws Exception {

		gpkiapi_jni gpki = this.getGPKI();
		try{
			int result = gpki.CMS_MakeSignedData(mySigCert, mySigKey, plain, null);
			checkResult(result, "Fail to sign message", gpki);
	
			return gpki.baReturnArray;
		}catch(Exception ex){
			throw ex;
		}finally{
			finish(gpki);
		}
	}

	public byte[] validate(byte[] signed) throws Exception {

		gpkiapi_jni gpki = this.getGPKI();
		try{
			int result = gpki.CMS_ProcessSignedData(signed);
			checkResult(result, "Fail to validate signed message", gpki);
			return gpki.baData;
		}catch(Exception ex){
			throw ex;			
		}finally{
			finish(gpki);
		}
	}

	public String encode(byte[] plain) throws Exception {

		gpkiapi_jni gpki = this.getGPKI();
		try{
			int result = gpki.BASE64_Encode(plain);
			checkResult(result, "Fail to encode message", gpki);
	
			return gpki.sReturnString;
		}catch(Exception ex){
			throw ex;
		}finally{
			finish(gpki);
		}
		
	}

	public byte[] decode(String base64) throws Exception {

		gpkiapi_jni gpki = this.getGPKI();
		try{
			int result = gpki.BASE64_Decode(base64);
			checkResult(result, "Fail to decode base64 message", gpki);
	
			return gpki.baReturnArray;
		}catch(Exception ex){
			throw ex;
		}finally{
			finish(gpki);
		}
	}
	
	private void checkResult(int result, gpkiapi_jni gpki)throws Exception{
		this.checkResult(result, null, gpki);
	}
	
	private void checkResult(int result ,String message,  gpkiapi_jni gpki)throws Exception{
		if( 0 != result){
			if(null != gpki){
				throw new Exception(message + " : gpkiErrorMessage=" + gpki.sDetailErrorString);
			}else{
				throw new Exception(message + " : gpkiErrorCode=" + result);
			}
		}
	}
	
	public void testGpki(gpkiapi_jni gpki) throws Exception{
		//gpki test eng
		log.debug("=======================================================");
		log.debug("================ TEST GPKI START ======================");
		log.debug("=======================================================");
		String original_Eng = "abc";
		log.debug("=== TEST ENG STRING: "+ original_Eng);
		try {
			byte[] encrypted = encrypt(original_Eng.getBytes(), myServerId);
			log.debug("=== TEST ENG ENCRYPT STRING: "+ encode(encrypted));
			String decrypted = new String(decrypt(encrypted));
			log.debug("=== TEST ENG DECRYPT STRING: "+decrypted);
			
			if (!original_Eng.equals(decrypted)) {
				throw new Exception("GpkiUtil not initialized properly(english)");
			}
			log.debug("=== TEST ENG: OK");
		} catch (Exception e) {
			log.warn("Gpki Test error(english)", e);
			throw e;
		}
		//gpki test kor
		String original = "한글테스트";
		log.debug("=== TEST KOR STRING: "+ original);
		try {
			byte[] encrypted = encrypt(original.getBytes(), myServerId);
			log.debug("=== TEST KOR ENCRYPT STRING: "+ encode(encrypted));
			String decrypted = new String(decrypt(encrypted));
			log.debug("=== TEST KOR DECRYPT STRING: "+decrypted);
			if (!original.equals(decrypted)) {
				throw new Exception("GpkiUtil not initialized properly(korean)");
			}
			log.debug("=== TEST KOR: OK");
		} catch (Exception e) {
			log.warn("Gpki Test error(korean)", e);
			throw e;
		}finally{
			log.debug("=======================================================");
			log.debug("================ TEST GPKI END ========================");
			log.debug("=======================================================");
		}
	}
	
//	public GpkiUtilRsultBean fileEncryptGenerate(GpkiUtilReqBean reqBean) throws GpkiApiException {
//		GpkiUtilRsultBean rstBean = null;
//		String orgFileFullPath = "";
//		String encFileFullPath = "";
//		String tmpFileFullPath = "";
//		String signedFileFullPath = "";
//		try {
//			rstBean = new GpkiUtilRsultBean();
//			if ((reqBean.getOrgFileName().equals("")) || (reqBean.getOrgFilePath().equals(""))) {
//				rstBean.setResultBln(false);
//				rstBean.setErrorMsg("Not found essential parameter [org file path or org file name]");
//				return rstBean;
//			}
//
//			if (reqBean.getEncFilePath().equals("")) {
//				reqBean.setEncFilePath(reqBean.getOrgFilePath());
//			}
//
//			if (reqBean.getEncFileName().equals("")) {
//				reqBean.setEncFileName(reqBean.getOrgFileName());
//			}
//
//			File encDir = new File(reqBean.getEncFilePath());
//			if (!encDir.exists()) {
//				encDir.mkdirs();
//			}
//
//			orgFileFullPath = reqBean.getOrgFilePath() + File.separator + reqBean.getOrgFileName();
//
//			if (reqBean.isKeepOrgFile()) {
//				tmpFileFullPath = reqBean.getEncFilePath() + File.separator + reqBean.getEncFileName();
//				copyFile(orgFileFullPath, tmpFileFullPath);
//			} else {
//				tmpFileFullPath = orgFileFullPath;
//			}
//
//			encFileFullPath = tmpFileFullPath + PREFIX_ENCRYPTED_EXTENTION;
//			signedFileFullPath = tmpFileFullPath + PREFIX_SIGNED_EXTENTION;
//
//			String targetServerId = "";
//			if(targetServerIdList!=null){
//				String certIdList[] = targetServerIdList.split(",");
//				for(int i = 0 ; i < certIdList.length ; i++){
//					String certId = certIdList[i].trim();
//					if(!certId.equals("")){
//						targetServerId = certId;
//					}
//				}
//			}
//			
//			encryptFile(tmpFileFullPath, encFileFullPath, targetServerId);
//			signFile(encFileFullPath, signedFileFullPath);
//
//			rstBean.setChgFileName(signedFileFullPath);
//
//			new File(tmpFileFullPath).delete();
//			new File(encFileFullPath).delete();
//
//			rstBean.setResultBln(true);
//		} catch (IOException ex) {
//			rstBean.setResultBln(false);
//			rstBean.setErrorMsg(ex.toString());
//		}
//		return rstBean;
//	}
//	
//	public void encryptFile(String sourceFilePath, String targetFilePath, String targetServerId) throws GpkiApiException {
//		X509Certificate targetEnvCert = (X509Certificate) this.targetServerCertMap.get(targetServerId);
//		if (targetEnvCert == null) {
//			throw new GpkiApiException("Certificate not found : targetServerId=" + targetServerId);
//		}
//
//		EnvelopedData data = new EnvelopedData("NEAT");
//		data.addRecipient(targetEnvCert);
//		data.generate_File(sourceFilePath, targetFilePath);
//	}
//
//	public void signFile(String sourceFilePath, String targetFilePath) throws GpkiApiException {
//		com.gpki.gpkiapi.cms.SignedData sdata = new com.gpki.gpkiapi.cms.SignedData();
//		sdata.setMessage_File(sourceFilePath);
//		sdata.generate_File(new X509Certificate(this.mySigCert), new com.gpki.gpkiapi.crypto.PrivateKey(this.mySigKey), targetFilePath);
//	}
//	
//	public void copyFile(String source, String dest) throws GpkiApiException, IOException {
//		File sourceFile = new File(source);
//
//		FileInputStream inputStream = null;
//		FileOutputStream outputStream = null;
//
//		try {
//			inputStream = new FileInputStream(sourceFile);
//			outputStream = new FileOutputStream(dest);
//
//			int bytesRead = 0;
//
//			byte[] buffer = new byte[1024];
//			while ((bytesRead = inputStream.read(buffer)) != -1) {
//				outputStream.write(buffer, 0, bytesRead);
//			}
//		} finally {
//			try {
//				if (outputStream != null)
//					outputStream.close();
//			} catch (IOException localIOException) {
//			}
//			try {
//				if (inputStream != null) {
//					inputStream.close();
//				}
//			} catch (IOException localIOException1) {
//			}
//		}
//	}
	
	public String getMyServerId() {
		return myServerId;
	}

	public void setMyServerId(String myServerId) {
		this.myServerId = myServerId.trim();
	}

	public String getEnvCertFilePathName() {
		return envCertFilePathName;
	}

	public void setEnvCertFilePathName(String envCertFilePathName) {
		this.envCertFilePathName = envCertFilePathName.trim();
	}

	public String getEnvPrivateKeyFilePathName() {
		return envPrivateKeyFilePathName;
	}

	public void setEnvPrivateKeyFilePathName(String envPrivateKeyFilePathName) {
		this.envPrivateKeyFilePathName = envPrivateKeyFilePathName.trim();
	}

	public String getEnvPrivateKeyPasswd() {
		return envPrivateKeyPasswd;
	}

	public void setEnvPrivateKeyPasswd(String envPrivateKeyPasswd) {
		this.envPrivateKeyPasswd = envPrivateKeyPasswd.trim();
	}

	public String getSigPrivateKeyPasswd() {
		return sigPrivateKeyPasswd;
	}

	public void setSigPrivateKeyPasswd(String sigPrivateKeyPasswd) {
		this.sigPrivateKeyPasswd = sigPrivateKeyPasswd.trim();
	}

	public String getSigCertFilePathName() {
		return sigCertFilePathName;
	}

	public void setSigCertFilePathName(String sigCertFilePathName) {
		this.sigCertFilePathName = sigCertFilePathName.trim();
	}

	public String getSigPrivateKeyFilePathName() {
		return sigPrivateKeyFilePathName;
	}

	public void setSigPrivateKeyFilePathName(String sigPrivateKeyFilePathName) {
		this.sigPrivateKeyFilePathName = sigPrivateKeyFilePathName.trim();
	}

	public boolean getIsLDAP() {
		return isLDAP;
	}

	public void setIsLDAP(boolean isLDAP) {
		this.isLDAP = isLDAP;
	}

	public String getCertFilePath() {
		return certFilePath;
	}

	public void setCertFilePath(String certFilePath) {
		this.certFilePath = certFilePath.trim();
	}

	public String getTargetServerIdList() {
		return targetServerIdList;
	}

	public void setTargetServerIdList(String targetServerIdList) {
		this.targetServerIdList = targetServerIdList;
	}

	public String getGpkiLicPath() {
		return gpkiLicPath;
	}

	public void setGpkiLicPath(String gpkiLicPath) {
		this.gpkiLicPath = gpkiLicPath;
	}

	public boolean getTestGPKI() {
		return testGPKI;
	}

	public void setTestGPKI(boolean testGPKI) {
		this.testGPKI = testGPKI;
	}


}

