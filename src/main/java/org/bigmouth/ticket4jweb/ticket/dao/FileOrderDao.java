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
import org.bigmouth.ticket4jweb.ticket.entity.Ticket4jOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


public class FileOrderDao extends BaseLifeCycleSupport implements OrderDao {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FileOrderDao.class);
    private static final String ORDER_DIR = PathUtils.appendEndFileSeparator(Ticket4jDefaults.PATH_TICKET4J) + "process";

    @Override
    public void insert(Ticket4jOrder ticket4jOrder) {
        if (null == ticket4jOrder)
            return;
        if (StringUtils.isBlank(ticket4jOrder.getId()))
            return;
        String id = ticket4jOrder.getId();
        File file = getFile(id);
        try {
            Ticket4jOutputStream.write(ticket4jOrder, file);
        }
        catch (IOException e) {
            throw new RuntimeException("订单任务添加失败!", e);
        }
    }

    @Override
    public void delete(String id) {
        File file = getFile(id);
        try {
            FileUtils.forceDelete(file);
        }
        catch (IOException e) {
            throw new RuntimeException("文件删除失败!", e);
        }
    }

    @Override
    public List<Ticket4jOrder> queryAll() {
        List<Ticket4jOrder> orders = Lists.newArrayList();
        File dir = new File(ORDER_DIR);
        File[] files = dir.listFiles();
        if (ArrayUtils.isEmpty(files))
            return orders;
        for (File file : files) {
            try {
                Ticket4jOrder order = Ticket4jOutputStream.read(file, false);
                orders.add(order);
            }
            catch (Exception e) {
                LOGGER.error("反序列化订单对象失败!", e);
            }
        }
        return orders;
    }
    
    @Override
    protected void doInit() {
        mkdirsIfNotExists();
    }

    @Override
    protected void doDestroy() {
    }
    
    private File getFile(String id) {
        File file = new File(PathUtils.appendEndFileSeparator(ORDER_DIR) + id);
        return file;
    }

    private void mkdirsIfNotExists() {
        File file = new File(ORDER_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
}
