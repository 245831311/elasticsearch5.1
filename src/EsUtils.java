import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkIndexByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;


public class EsUtils {
	
	private static TransportClient client = EsConnectionFactory.client;
	
	public static void main(String[] args) {
		deleteAllIndex();
		//createIndex("jay2");
		//delIndex("jay2");
		//createMapping("jay","test3");
		//insertValue("jay","test2");
		//updateValue("jay","test2","AVxLJTcuPDAe1x9hgRqG");
		//queryById("jay","test2","AVxLO8D7PDAe1x9hgRqN");
		//deleteByQuery("jay","test2");
		//queryByFilter("jay","test2");
	}
	
	/**
	 * 判断是否存在index
	 */
	private static boolean isIndexExist(String indexName){
	        IndicesExistsResponse inExistsResponse = client
	        		.admin()
	        		.indices()
	        		.exists(new IndicesExistsRequest(indexName))
	                .actionGet();
	        return inExistsResponse.isExists();
	}
	
	
	/**
	 * 1.创建索引
	 */
	public static void createIndex(String indexName){
		try{
			if(!isIndexExist(indexName)){
				CreateIndexResponse response = client.admin().indices().prepareCreate(indexName).get();
				//表示是否成功
				System.out.println("创建成功--->"+indexName+":"+response.isAcknowledged());
			}else{
				System.out.println("创建失败--->"+indexName+"已存在");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 2.删除索引
	 */
	public static void delIndex(String indexName){
		try{
			if(isIndexExist(indexName)){
				DeleteIndexResponse response = client.admin().indices().prepareDelete(indexName).get();
				System.out.println("删除成功--->"+indexName+":"+response.isAcknowledged());
			}else{
				System.out.println("删除失败--->"+indexName+"不存在");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 删除所有索引
	 */
	public static void deleteAllIndex() {
		ClusterStateResponse response = client.admin().cluster().prepareState().execute().actionGet();
		// 获取所有索引
		String[] indexs = response.getState().getMetaData().getConcreteAllIndices();
		for (String index : indexs) {
			// 清空所有索引。
			DeleteIndexResponse deleteIndexResponse = client.admin().indices().prepareDelete(index).execute()
					.actionGet();
			if (deleteIndexResponse.isAcknowledged()) {
				System.out.println(index + " delete");//
			}

		}
	}
	
	/**
	 * 3.创建mapping
	 */
	public static void createMapping(String indexName,String type){
		try{
			XContentBuilder builder = XContentFactory.jsonBuilder()
											.startObject()
						                    .field("properties")
						                        .startObject()
						                            .field("title")
						                                .startObject()
						                                    .field("type", "string")
						                                .endObject()
						                            .field("age")
						                                .startObject()
						                                    .field("index", "not_analyzed")
						                                    .field("type", "integer")
						                                .endObject()
						                            .field("name")
						                                .startObject()
						                                    .field("type", "integer")
						                                .endObject()
						                             .endObject()
											.endObject();
			 System.out.println(builder.string());           
		     PutMappingRequest mappingRequest = Requests.putMappingRequest(indexName).source(builder).type(type);
		     client.admin().indices().putMapping(mappingRequest).actionGet();
					
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 4.插入数值
	 */
	public static void insertValue(String indexName,String type){
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
			                .startObject()
			                    .field("name", "zhangsan")
			                    .field("age", 30)
			                .endObject();
			IndexResponse  indexResponse = client.prepareIndex()
                  .setIndex(indexName)
                  .setType(type)
                  .setSource(builder.string())
                  .get();
			System.out.println(indexResponse.status());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * 5.更新记录
	 */
	public static void updateValue(String indexName,String type,String id){
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
			                .startObject()
			                    .field("name", "jj")
			                .endObject();
			UpdateResponse updateResponse = 
		            	client
		                .prepareUpdate()
		                .setIndex(indexName)
		                .setType(type)
		                .setId(id)
		                .setDoc(builder.string())
		                .get();
			System.out.println(updateResponse.status());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * 6.删除某条记录
	 */
	public static void delRecord(String indexName,String type,String id){
		try{
			//删除方法一
			DeleteResponse deleteResponse  = client
		            .prepareDelete()  
		            .setIndex(indexName)
		            .setType(type)
		            .setId(id)
		            .get();
			//删除方法二
			/*client.prepareDelete(indexName, type, id).get();*/
		    System.out.println(deleteResponse.status()); // true表示成功
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 7.根据条件删除记录
	 */
	public static void deleteByQuery(String index, String type) {
		try{
			BulkIndexByScrollResponse response = DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
					.filter(QueryBuilders.matchQuery("name", "zhangsan")).source(index).get();

			System.out.println(response.getDeleted());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 8.根据id查找记录
	 */
	public static void queryById(String indexName,String type,String id){
		try{
			GetResponse getResponse = client
                    .prepareGet()   
                    .setIndex(indexName)  
                    .setType(type)
                    .setId(id)
                    .get();
			System.out.println(getResponse.toString());
			System.out.println("getSource:"+getResponse.getSourceAsMap().toString());
			System.out.println("getType:"+getResponse.getType());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 9.根据条件查询
	 */
	public static void queryByFilter(String indexName, String type) {
		try{
			QueryBuilder queryBuilder = QueryBuilders.termQuery("name", "zhangsan");
			SearchResponse response = client.prepareSearch(indexName).setTypes(type)
			        .setQuery(queryBuilder)
			        .execute()
			        .actionGet();
			List<String> docList = new ArrayList<String>();
			SearchHits searchHits = response.getHits();
		    for (SearchHit hit : searchHits) {
		        docList.add(hit.getSourceAsString());
		    }
		    System.out.println(docList.toString());
		    System.out.println(response.getTookInMillis());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
