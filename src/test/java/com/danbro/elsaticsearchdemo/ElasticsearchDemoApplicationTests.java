package com.danbro.elsaticsearchdemo;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class ElasticsearchDemoApplicationTests {

    @Autowired
    @Qualifier("getRestHighLevelClient")
    private RestHighLevelClient client;

    /**
     * 创建索引
     */
    @Test
    void createIndex() throws IOException {
        // 创建索引的请求，指定索引名
        CreateIndexRequest indexRequest = new CreateIndexRequest("ems");
        CreateIndexResponse response = client.indices().create(indexRequest, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());
    }

    /**
     * 删除索引
     */
    @Test
    void deleteIndex() throws IOException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("ems");
        AcknowledgedResponse acknowledgedResponse = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        System.out.println(acknowledgedResponse.isAcknowledged());
    }

    /**
     * 创建索引并指定Mapping
     */
    @Test
    void createIndexAndMapping() throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.startObject("properties");
            {
                builder.startObject("name");
                {
                    builder.field("type", "text").
                            field("analyzer", "ik_smart");
                }
                builder.endObject();
                builder.startObject("age");
                {
                    builder.field("type", "integer");
                }
                builder.endObject();
                builder.startObject("bir");
                {
                    builder.field("type", "date");
                }
                builder.endObject();
                builder.startObject("content");
                {
                    builder.field("type", "text").
                            field("analyzer", "ik_smart");
                }
                builder.endObject();
                builder.startObject("address");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
            }
            builder.endObject();
        }
        builder.endObject();

        CreateIndexRequest indexRequest = new CreateIndexRequest("ems");
        indexRequest.mapping(builder);
        AcknowledgedResponse response = client.indices().create(indexRequest, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());
    }
}
