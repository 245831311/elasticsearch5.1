import java.net.InetAddress;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

public class EsConnectionFactory {

	private final static String CLUSTER_NAME = "myEs-test";
	//�ɴ������ļ��ж���
	private final static String ADDRESS = "127.0.0.1:9300";
	
	public static TransportClient client = null;
	
	static{
		try{
			
			String[] addresses = ADDRESS.split(",");
			Settings settings = Settings.builder()
					//����ES��Ⱥ����
					.put("cluster.name", CLUSTER_NAME)
					////�Զ���̽������Ⱥ��״̬���Ѽ�Ⱥ������ES�ڵ��ip��ӵ����صĿͻ����б���
					.put("client.transport.sniff", false).build();
			client = new PreBuiltTransportClient(settings);
			for (int i = 0; i < addresses.length; i++) {
				client.addTransportAddress(
						new InetSocketTransportAddress(InetAddress.getByName(addresses[i].split(":")[0]),
								Integer.parseInt(addresses[i].split(":")[1])));
			}
		}catch(Exception e){
			//��ӡ��־
			e.printStackTrace();
		}
	}
	
}
