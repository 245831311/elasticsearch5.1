import java.net.InetAddress;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

public class EsConnectionFactory {

	private final static String CLUSTER_NAME = "myEs-test";
	//可从配置文件中读出
	private final static String ADDRESS = "127.0.0.1:9300";
	
	public static TransportClient client = null;
	
	static{
		try{
			
			String[] addresses = ADDRESS.split(",");
			Settings settings = Settings.builder()
					//设置ES集群名称
					.put("cluster.name", CLUSTER_NAME)
					////自动嗅探整个集群的状态，把集群中其他ES节点的ip添加到本地的客户端列表中
					.put("client.transport.sniff", false).build();
			client = new PreBuiltTransportClient(settings);
			for (int i = 0; i < addresses.length; i++) {
				client.addTransportAddress(
						new InetSocketTransportAddress(InetAddress.getByName(addresses[i].split(":")[0]),
								Integer.parseInt(addresses[i].split(":")[1])));
			}
		}catch(Exception e){
			//打印日志
			e.printStackTrace();
		}
	}
	
}
