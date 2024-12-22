//package com.supercharge.gateway.utils.core;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Vector;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.stereotype.Component;
//import org.springframework.web.multipart.MultipartFile;
//
//import com.cbt.supercharge.constants.core.ApplicationConstants;
//import com.cbt.supercharge.exception.core.ApplicationException;
//import com.cbt.supercharge.exception.core.codes.ErrorCodes;
//import com.cbt.supercharge.utils.core.SftpService;
//import com.jcraft.jsch.ChannelSftp;
//import com.jcraft.jsch.SftpException;
//
//@Component
//public class FileServiceUtils {
//
//	/**
//	 * SftpService
//	 */
//	@Autowired
//	private SftpService sftpService;
//
//	/**
//	 * Logger
//	 */
//	private static final Logger logger = LoggerFactory.getLogger(FileServiceUtils.class);
//
//	
//	/**
//	 * @param fileName
//	 * @param templateName
//	 * @return
//	 * @throws Exception
//	 */
//	public byte[] readFromLocalServer(String fileName, String templateName) throws Exception {
//		ChannelSftp channelSftp = null;
//		try {
//			String localDir = getSftpDirectoryPath(templateName, ApplicationConstants.FILE_REPOSITORY_CONFIGURATION);
//			// Establish SFTP connection
//			channelSftp = sftpService.connectAndGetChannelSftp(ApplicationConstants.FILE_REPOSITORY_CONFIGURATION);
//			InputStream inputStream = channelSftp.get(localDir + ApplicationConstants.SLASH + fileName);
//			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//			byte[] buffer = new byte[1024];
//			int len;
//			while ((len = inputStream.read(buffer)) > 0) {
//				byteArrayOutputStream.write(buffer, 0, len);
//			}
//			logger.info("File downloaded successfully: " + localDir);
//			return byteArrayOutputStream.toByteArray();
//		} finally {
//			sftpService.disconnect(channelSftp);
//		}
//	}
//
//	
//	/**
//	 * @param inputStream
//	 * @param folderName
//	 * @param fileName
//	 * @throws Exception
//	 */
//	public void loadFileIntoLocalServer(InputStream inputStream, String folderName, String fileName) throws Exception {
//		ChannelSftp channelSftp = null;
//		try {
//			String directoryPath = getSftpDirectoryPath(folderName, ApplicationConstants.FILE_REPOSITORY_CONFIGURATION);
//			channelSftp = sftpService.connectAndGetChannelSftp(ApplicationConstants.FILE_REPOSITORY_CONFIGURATION);
//			channelSftp.cd(directoryPath);
//			channelSftp.put(inputStream, fileName);
//			logger.info("File uploaded successfully to: " + directoryPath);
//		} catch (SftpException e) {
//			throw new ApplicationException(ErrorCodes.FILE_NOT_FOUND);
//		} finally {
//			sftpService.disconnect(channelSftp);
//		}
//	}
//
//	
//	/**
//	 * @param folderName
//	 * @param fileName
//	 * @throws Exception
//	 */
//	public void removeProcessedFileFromRemoteServer(String folderName, String fileName) throws Exception {
//		ChannelSftp channelSftp = null;
//		try {
//			String directoryPath = getSftpDirectoryPath(folderName, ApplicationConstants.HAND_OFF_FILE_CONFIGURATION);
//			channelSftp = sftpService.connectAndGetChannelSftp(ApplicationConstants.HAND_OFF_FILE_CONFIGURATION);
//			String remoteFilePath = directoryPath + ApplicationConstants.SLASH + fileName;
//			channelSftp.rm(remoteFilePath);
//			logger.info("File removed from remote server: " + remoteFilePath);
//		} finally {
//			sftpService.disconnect(channelSftp);
//		}
//	}
//
//	
//	/**
//	 * @param folderName
//	 * @return
//	 * @throws Exception
//	 */
//	public List<MultipartFile> listRemoteFiles(String folderName) throws Exception {
//		List<MultipartFile> fileList = new ArrayList<>();
//		ChannelSftp channelSftp = null;
//		try {
//			String remoteDir = getSftpDirectoryPath(folderName, ApplicationConstants.HAND_OFF_FILE_CONFIGURATION);
//			channelSftp = sftpService.connectAndGetChannelSftp(ApplicationConstants.HAND_OFF_FILE_CONFIGURATION);
//			Vector<ChannelSftp.LsEntry> remoteFiles = channelSftp.ls(remoteDir);
//			for (ChannelSftp.LsEntry entry : remoteFiles) {
//				if (!entry.getAttrs().isDir()) {
//					InputStream inputStream = channelSftp.get(remoteDir + ApplicationConstants.SLASH + entry.getFilename());
//					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//					byte[] buffer = new byte[1024];
//					int len;
//					while ((len = inputStream.read(buffer)) != -1) {
//						byteArrayOutputStream.write(buffer, 0, len);
//					}
//					fileList.add(new MockMultipartFile(entry.getFilename(), new ByteArrayInputStream(byteArrayOutputStream.toByteArray())));
//				}
//			}
//		} finally {
//			sftpService.disconnect(channelSftp);
//		}
//		return fileList;
//	}
//
//	/**
//	 * @param templateName
//	 * @param connectionType
//	 * @return
//	 * @throws Exception
//	 */
//	public String getSftpDirectoryPath(String templateName, String connectionType) throws Exception {
//		Map<String, String> configDetails = sftpService.getFileServerConfig(connectionType);
//		return configDetails.get(ApplicationConstants.DIRECTORY) + templateName;
//	}
//
//}
//
