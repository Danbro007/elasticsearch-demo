package com.danbro.elsaticsearchdemo;

import com.alibaba.fastjson.JSON;
import com.danbro.elsaticsearchdemo.entity.User;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Date;

@SpringBootTest
class DocumentTest {

    @Autowired
    @Qualifier("getRestHighLevelClient")
    private RestHighLevelClient client;

    /**
     * 添加文档,指定文档的ID
     */
    @Test
    void createDocumentOptionId() throws IOException {
        User user = new User("josh", 28, new Date(), "内容123", "上海");
        IndexRequest request = new IndexRequest("ems");
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));
        request.source(JSON.toJSONString(user), XContentType.JSON);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
        System.out.println(response.status());
    }

    /**
     * 添加文档，自动生成文档 ID。
     */
    @Test
    void createDocumentAutoId() throws IOException {
        User user = new User("Wang", 76, new Date(), "自动生成ID的内容2", "北京");
        IndexRequest request = new IndexRequest("ems");
        request.timeout(TimeValue.timeValueSeconds(1));
        request.source(JSON.toJSONStringWithDateFormat(user, "yyyy-MM-dd"), XContentType.JSON);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
        System.out.println(response.status());
    }

    /**
     * 通过文档ID查找文档
     */
    @Test
    void getDocument() throws IOException {
        // 指定要查询的索引和文档ID
        GetRequest request = new GetRequest("ems", "1");
        GetResponse getResponse = client.get(request, RequestOptions.DEFAULT);
        System.out.println(getResponse);
        // 打印文档内容
        System.out.println(getResponse.getSourceAsString());
    }

    /**
     * 查看文档是否存在
     */
    @Test
    void getDocumentIsExist() throws IOException {
        GetRequest request = new GetRequest("ems", "1");
        boolean exists = client.exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    /**
     * 更新文档
     */
    @Test
    void updateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("ems", "1");
        request.timeout(TimeValue.timeValueSeconds(1));
        User user = new User("Shan", 22);
        request.doc(JSON.toJSONString(user), XContentType.JSON);
        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
        System.out.println(response.status());
    }

    /**
     * 删除文档
     */
    @Test
    void deleteDocument() throws IOException {
        DeleteRequest request = new DeleteRequest("ems", "1");
        DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(response.status());
    }

    // 查找所有文档
    @Test
    void queryAllDocument() throws IOException {
        SearchRequest request = new SearchRequest("ems");
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // 查找 name 为 josh 的文档
//        TermQueryBuilder queryBuilder = QueryBuilders.termQuery("name", "josh");
        // 查找所有文档
        MatchAllQueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
        builder.timeout(TimeValue.timeValueSeconds(1));
        builder.query(queryBuilder);
        builder.sort("age");
        request.source(builder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        System.out.println(response.getHits());
        for (SearchHit documentFields : response.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap());
        }
    }

    /**
     * 批量操作
     */
    @Test
    void bulkDocument() throws IOException {
        // 添加文档
        User user = new User("李大旺", 29);
        IndexRequest indexRequest = new IndexRequest("ems");
        indexRequest.source(JSON.toJSONStringWithDateFormat(user, "yyyy-HH-dd"), XContentType.JSON);
        // 更新文档
        UpdateRequest updateRequest = new UpdateRequest("ems", "1");
        User UpdateUser = new User("达摩", 22);
        updateRequest.doc(JSON.toJSONString(UpdateUser), XContentType.JSON);
        // 删除文档
        DeleteRequest deleteRequest = new DeleteRequest("ems", "AcHk1HUBYRbHdQ73OQYX");
        // 批量执行
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.add(indexRequest)
                .add(updateRequest)
                .add(deleteRequest);

        BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        BulkItemResponse[] items = response.getItems();
        // 查看每个步骤的执行结果
        for (BulkItemResponse item : items) {
            System.out.println(item.status());
        }

    }
}
