package com.hui.gmall.manage;


import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
class GmallManageWebApplicationTests {


    @Test
    void contextLoads() throws IOException, MyException {
        String tracker = GmallManageWebApplication.class.getResource("/tracker.conf").getPath();  //获取配置文件路径

        ClientGlobal.init(tracker);

        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getTrackerServer();
        StorageClient storageClient = new StorageClient(trackerServer, null);

        String url = "http://192.168.159.134";

        String[] uploadInfos = storageClient.upload_file("D://abc.jpg", "jpg", null);

        for (String uploadInfo : uploadInfos) {
            url+=uploadInfo;
        }

        System.out.println(url);

    }

}
