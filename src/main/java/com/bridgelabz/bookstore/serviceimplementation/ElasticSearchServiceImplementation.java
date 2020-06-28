package com.bridgelabz.bookstore.serviceimplementation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bridgelabz.bookstore.configuration.ElasticSearchConfig;
import com.bridgelabz.bookstore.model.BookModel;
import com.bridgelabz.bookstore.service.ElasticSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ElasticSearchServiceImplementation implements ElasticSearchService {

	@Autowired
	ElasticSearchConfig elasticSearchConfig;

	@Autowired
	private ObjectMapper objectMapper;

	private static final String INDEX = "springboot";

	private static final String TYPE = "note_details";

	@Override
	public String addBook(BookModel bookModel) {

		Map<String, Object> notemapper = objectMapper.convertValue(bookModel, Map.class);
		IndexRequest indexrequest = new IndexRequest(INDEX, TYPE, String.valueOf(bookModel.getBookId()))
				.source(notemapper);
		IndexResponse indexResponse = null;
		try {
			indexResponse = elasticSearchConfig.client().index(indexrequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			log.info(e.getMessage());
		}
		log.info(indexrequest);
		log.info(indexResponse);
		return indexResponse.getResult().name();
	}
		
}
