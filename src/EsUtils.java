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
	 * �ж��Ƿ����index
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
	 * 1.��������
	 */
	public static void createIndex(String indexName){
		try{
			if(!isIndexExist(indexName)){
				CreateIndexResponse response = client.admin().indices().prepareCreate(indexName).get();
				//��ʾ�Ƿ�ɹ�
				System.out.println("�����ɹ�--->"+indexName+":"+response.isAcknowledged());
			}else{
				System.out.println("����ʧ��--->"+indexName+"�Ѵ���");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 2.ɾ������
	 */
	public static void delIndex(String indexName){
		try{
			if(isIndexExist(indexName)){
				DeleteIndexResponse response = client.admin().indices().prepareDelete(indexName).get();
				System.out.println("ɾ���ɹ�--->"+indexName+":"+response.isAcknowledged());
			}else{
				System.out.println("ɾ��ʧ��--->"+indexName+"������");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * ɾ����������
	 */
	public static void deleteAllIndex() {
		ClusterStateResponse response = client.admin().cluster().prepareState().execute().actionGet();
		// ��ȡ��������
		String[] indexs = response.getState().getMetaData().getConcreteAllIndices();
		for (String index : indexs) {
			// �������������
			DeleteIndexResponse deleteIndexResponse = client.admin().indices().prepareDelete(index).execute()
					.actionGet();
			if (deleteIndexResponse.isAcknowledged()) {
				System.out.println(index + " delete");//
			}

		}
	}
	
	/**
	 * 3.����mapping
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
	 * 4.������ֵ
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
	 * 5.���¼�¼
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
	 * 6.ɾ��ĳ����¼
	 */
	public static void delRecord(String indexName,String type,String id){
		try{
			//ɾ������һ
			DeleteResponse deleteResponse  = client
		            .prepareDelete()  
		            .setIndex(indexName)
		            .setType(type)
		            .setId(id)
		            .get();
			//ɾ��������
			/*client.prepareDelete(indexName, type, id).get();*/
		    System.out.println(deleteResponse.status()); // true��ʾ�ɹ�
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 7.��������ɾ����¼
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
	 * 8.����id���Ҽ�¼
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
	 * 9.����������ѯ
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
