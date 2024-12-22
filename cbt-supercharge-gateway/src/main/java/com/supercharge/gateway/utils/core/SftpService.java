//package com.supercharge.gateway.utils.core;
//
//import java.util.Map;
//import java.util.Properties;
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.cbt.supercharge.constants.core.ApplicationConstants;
//import com.cbt.supercharge.exception.core.ApplicationException;
//import com.cbt.supercharge.exception.core.codes.ErrorCodes;
//import com.cbt.supercharge.sftp.dao.ISftpDao;
//import com.cbt.supercharge.utils.core.ApplicationUtils;
//import com.jcraft.jsch.ChannelSftp;
//import com.jcraft.jsch.JSch;
//import com.jcraft.jsch.JSchException;
//import com.jcraft.jsch.Session;
//
//@Service
//public class SftpService {
//
//	@Autowired
//	private ISftpDao iSftpDao;
//
//	/**
//	 * @param serverReference
//	 * @return
//	 * @throws Exception
//	 */
//	public ChannelSftp connectAndGetChannelSftp(String serverReference) throws Exception {
//		Map<String, String> systemConfigDetails = iSftpDao.getFileServerConfiguration(List.of(serverReference));
//		if (!ApplicationUtils.isValidateObject(systemConfigDetails)) {
//			throw new ApplicationException(ErrorCodes.INVALID_OPERATION);
//		}
//		// Create a new session for this request
//		Session session = createSession(systemConfigDetails);
//		session.connect(ApplicationConstants.SESSION_CONNECT_TIMEOUT);
//		// Open a new SFTP channel
//		ChannelSftp channelSftp = (ChannelSftp) session.openChannel(ApplicationConstants.SFTP_CHANNEL_TYPE);
//		channelSftp.connect(ApplicationConstants.CHANNEL_CONNECT_TIMEOUT);
//		return channelSftp;
//	}
//
//	/**
//	 * Create a new session based on the system configuration details
//	 *
//	 * @param systemConfigDetails
//	 * @return Session
//	 * @throws JSchException
//	 */
//	private Session createSession(Map<String, String> systemConfigDetails) throws JSchException {
//		String host = systemConfigDetails.get(ApplicationConstants.HOST);
//		int port = Integer.parseInt(systemConfigDetails.get(ApplicationConstants.PORT));
//		String user = systemConfigDetails.get(ApplicationConstants.USER);
//		String password = systemConfigDetails.get(ApplicationConstants.PASSWORD);
//		JSch jsch = new JSch();
//		Session session = jsch.getSession(user, host, port);
//		session.setPassword(password);
//		// Set session properties
//		Properties config = new Properties();
//		config.put(ApplicationConstants.STRICT_HOST_KEY_CHECKING, ApplicationConstants.STRICT_HOST_KEY_CHECKING_NO);
//		session.setConfig(config);
//		return session;
//	}
//
//	/**
//	 * @param serverReference
//	 * @return
//	 * @throws Exception
//	 */
//	public Map<String, String> getFileServerConfig(String serverReference) throws Exception {
//		Map<String, String> systemConfigDetails = iSftpDao.getFileServerConfiguration(List.of(serverReference));
//		if (!ApplicationUtils.isValidateObject(systemConfigDetails)) {
//			throw new ApplicationException(ErrorCodes.INVALID_OPERATION);
//		}
//		return systemConfigDetails;
//	}
//
//	/**
//	 * @param channelSftp
//	 * @param session
//	 */
//	public void disconnect(ChannelSftp channelSftp) {
//		if (channelSftp != null && channelSftp.isConnected()) {
//			channelSftp.disconnect();
//		}
//		
//}
//}
