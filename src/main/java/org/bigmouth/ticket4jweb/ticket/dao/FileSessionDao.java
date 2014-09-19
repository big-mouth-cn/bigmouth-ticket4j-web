package org.bigmouth.ticket4jweb.ticket.dao;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.framework.util.BaseLifeCycleSupport;
import org.bigmouth.framework.util.PathUtils;
import org.bigmouth.ticket4j.Ticket4jDefaults;
import org.bigmouth.ticket4j.utils.Ticket4jOutputStream;
import org.bigmouth.ticket4jweb.ticket.entity.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


public class FileSessionDao extends BaseLifeCycleSupport implements SessionDao {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSessionDao.class);
    private static final String SESSION_DIR = PathUtils.appendEndFileSeparator(Ticket4jDefaults.PATH_TICKET4J) + "session";

    @Override
    public void insert(Session session) {
        if (null == session) 
            return;
        if (StringUtils.isBlank(session.getUsername())) 
            return;
        File file = getFile(session.getUsername());
        try {
            Ticket4jOutputStream.write(session, file);
        }
        catch (IOException e) {
            throw new RuntimeException("会话添加失败!", e);
        }
    }

    @Override
    public void delete(String username) {
        File file = getFile(username);
        try {
            FileUtils.forceDelete(file);
        }
        catch (IOException e) {
            throw new RuntimeException("文件删除失败!", e);
        }
    }

    @Override
    public List<Session> queryAll() {
        List<Session> sessions = Lists.newArrayList();
        File dir = new File(SESSION_DIR);
        File[] files = dir.listFiles();
        if (ArrayUtils.isEmpty(files))
            return sessions;
        for (File file : files) {
            try {
                Session session = Ticket4jOutputStream.read(file, false);
                sessions.add(session);
            }
            catch (Exception e) {
                LOGGER.error("反序列化会话对象失败!", e);
            }
        }
        return sessions;
    }

    @Override
    protected void doInit() {
        mkdirsIfNotExists();
    }

    @Override
    protected void doDestroy() {
    }
    
    private File getFile(String username) {
        return new File(PathUtils.appendEndFileSeparator(SESSION_DIR) + username);
    }
    
    private void mkdirsIfNotExists() {
        File file = new File(SESSION_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
}
