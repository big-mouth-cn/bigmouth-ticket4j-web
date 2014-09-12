/*
 * 文件名称: FileService.java
 * 版权信息: Copyright 2005-2014 Allen.Hu Inc. All right reserved.
 * ----------------------------------------------------------------------------------------------
 * 修改历史:
 * ----------------------------------------------------------------------------------------------
 * 修改原因: 新增
 * 修改人员: Allen.Hu
 * 修改日期: 2014-4-7
 * 修改内容: 
 */
package org.bigmouth.ticket4jweb.commons;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.struts2.ServletActionContext;
import org.bigmouth.framework.session.util.UUIDUtils;
import org.bigmouth.framework.util.DateUtils;
import org.bigmouth.framework.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件存储服务
 * 
 * @author Allen.Hu / 2014-4-7
 */
public class FileService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    private static final String ATTACHED_NAME = "attached";
    
    private static final String SPLIT = "/";
    
    public String store(File imgFile, String imgFileName) throws FileUploadException {
        String saveHolder = getFileSaveHolder();
        String browseURL = getFileBrowseURL();
        File file = new File(saveHolder);
        if (!file.exists()) {
            boolean flag = file.mkdirs();
            if (!flag) {
                throw new FileUploadException(saveHolder + " create failed.");
            }
        }
        String fileName = getFileName(imgFileName);
        File img = new File(saveHolder + fileName);
        FileUtils.copy(imgFile, img);
        return browseURL + fileName;
    }
    
    public void delete(String relatively) {
        String holder = getRealPath();
        try {
            File file = new File(holder + relatively);
            org.apache.commons.io.FileUtils.forceDelete(file);
            LOGGER.info("Delete file: " + file.getPath());
        }
        catch (IOException e) {
            LOGGER.error("delete: ", e);
        }
    }

    /**
     * <pre>
     * e.g. /bigmouth-web/attached/20130623/
     * </pre>
     * 
     * @return
     */
    private String getFileBrowseURL() {
        String url = ServletActionContext.getRequest().getContextPath() + SPLIT + ATTACHED_NAME + SPLIT;
        url += getFolder() + SPLIT;
        return url;
    }

    /**
     * <pre>
     * D:\Workspace\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\bigmouth-web\attached\20130623
     * </pre>
     * 
     * @return
     */
    public String getFileSaveHolder() {
        String savePath = getRealPath() + ATTACHED_NAME + SPLIT;
        savePath += getFolder() + SPLIT;
        return savePath;
        // String savePath = SpringContextProperty.getContextProperty("file.save.path");
        // if (StringUtils.lastIndexOf(savePath, "/") != -1) {
        // savePath += SPLIT;
        // }
        // savePath += getFolder() + SPLIT;
        // return savePath;
    }
    
    private String getRealPath() {
        return ServletActionContext.getServletContext().getRealPath(SPLIT) + SPLIT;
    }

    /**
     * <pre>
     * 68C9A155CF1F487AA84ED6EEC721328A.jpg
     * </pre>
     * 
     * @return
     */
    private String getFileName(String imgFileFileName) {
        String ext = imgFileFileName.substring(imgFileFileName.lastIndexOf("."));
        String fileName = UUIDUtils.getUUID() + ext;
        return fileName;
    }

    private String getFolder() {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.YYYYMMDD);
        String ymd = sdf.format(new Date());
        return ymd;
    }
}
