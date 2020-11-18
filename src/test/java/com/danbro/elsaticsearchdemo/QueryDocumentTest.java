package com.danbro.elsaticsearchdemo;

import com.sun.org.apache.regexp.internal.RE;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.ml.GetFiltersRequest;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@SpringBootTest
class QueryDocumentTest {
    @Autowired
    @Qualifier("getRestHighLevelClient")
    private RestHighLevelClient client;

    /**
     * 执行搜索
     *
     * @param queryBuilder 搜索条件
     */
    public void doSearch(QueryBuilder queryBuilder) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SearchRequest searchRequest = new SearchRequest("ems");
        searchSourceBuilder.query(queryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        Arrays.stream(searchHits).forEach(e -> System.out.println(e.getSourceAsMap()));
    }

    /**
     * 查询所有文档
     */
    @Test
    void queryAllDocument() throws IOException {
        MatchAllQueryBuilder matchAllQuery = QueryBuilders.matchAllQuery();
        doSearch(matchAllQuery);
    }

    /**
     * 关键词查找
     */
    @Test
    void queryTermDocument() throws IOException {
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("content", "框架");
        doSearch(termQueryBuilder);
    }

    /**
     * 通配符 ? or *
     */
    @Test
    void wildcardTermDocument() throws IOException {
        WildcardQueryBuilder wildcardQuery = QueryBuilders.wildcardQuery("name", "小?");
        doSearch(wildcardQuery);
    }

    /**
     * 范围查询
     */
    @Test
    void rangeDocument() throws IOException {
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("age").gt(10).lt(30);
        doSearch(rangeQueryBuilder);
    }

    /**
     * 前缀查询
     */
    @Test
    void prefixDocument() throws IOException {
        PrefixQueryBuilder prefixQueryBuilder = QueryBuilders.prefixQuery("content", "框");
        doSearch(prefixQueryBuilder);
    }

    /**
     * Ids 查询
     */
    @Test
    void IdsDocument() throws IOException {
        IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery().addIds("HMEU1XUBYRbHdQ73AwY9")
                .addIds("HcEU1XUBYRbHdQ73AwY9")
                .addIds("IMEU1XUBYRbHdQ73AwY9");
        doSearch(idsQueryBuilder);
    }

    /**
     * fuzzy 模糊查询
     */
    @Test
    void fuzzyDocument() throws IOException {
        FuzzyQueryBuilder fuzzyQueryBuilder = QueryBuilders.fuzzyQuery("content", "sproog");
        doSearch(fuzzyQueryBuilder);
    }

    /**
     * bool 查询
     */
    @Test
    void boolDocument() throws IOException {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.should(QueryBuilders.matchAllQuery());
        // 内容有 框架 二字
        boolQueryBuilder.must(QueryBuilders.termQuery("content", "框架"));
        // 地址不能是 北京
        boolQueryBuilder.mustNot(QueryBuilders.termQuery("address", "北京"));
        doSearch(boolQueryBuilder);
    }

    /**
     * 关键词高亮
     */
    @Test
    void highlightDocument() throws IOException {
        SearchRequest searchRequest = new SearchRequest("ems");
        // 指定关键词并且指定在哪些字段查找
        MultiMatchQueryBuilder queryBuilder = QueryBuilders.multiMatchQuery("开源", "content", "name");
        // 高亮设置，指定要高亮的字段，样式以及关闭字段匹配功能，如果开启的话只会高亮一个最匹配的字段。
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("content")
                .field("name")
                .preTags("<span style='color:red'>")
                .postTags("</span>")
                .requireFieldMatch(false);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.highlighter(highlightBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            // 每个文档的高亮属性
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            System.out.println(highlightFields.get("content"));
            System.out.println(highlightFields.get("name"));
        }
    }

    /**
     * 过滤器查找
     * 先用过滤器找到 age 大于 15 的文档，然后在结果里过滤出 content 或 name 包含 ”开源“ 的文档
      */
    @Test
    void filterDocument() throws IOException {
        SearchRequest searchRequest = new SearchRequest("ems");
        searchRequest.source(SearchSourceBuilder.searchSource()
                .query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.multiMatchQuery("开源", "content", "name"))
                        .filter(QueryBuilders.rangeQuery("age").gt(15))));
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();
        Arrays.stream(hits).forEach(e -> System.out.println(e.getSourceAsString()));
    }
}
