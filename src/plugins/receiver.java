package plugins;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.*;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.Random;

//在原socket被打断时（无论是强制继承还是断开连接，此时receiver在session中的引用已经被取消）
//receiver都会开始向buffer写入消息（只可能在preparing状态才有的动作）直到读取到结束标志（读到消息-检查状态-解析消息）
//然后自我消亡
public class receiver {
	private String host;
	private String targetExchanger;
	private session masterSession;
	private Channel channel;
	private String queueName;
	private boolean working = false;
	
	public receiver(String host,String targetExchanger,session masterSession) throws Exception{
		this.host = host;
		this.targetExchanger = targetExchanger;
		this.masterSession = masterSession;
		
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost(this.host);
		Connection connection = connectionFactory.newConnection();
	    channel = connection.createChannel();
	    
	    String userId_str = Integer.toString(this.masterSession.getUserId());
	    queueName = userId_str+"_recvQueue_"+Integer.toString(new Random().nextInt(100000));
	    channel.queueDeclare(queueName, false, false, true, null);//true是自动清理
		channel.queueBind(queueName, this.targetExchanger, userId_str);
		channel.basicQos(1);
	}
	
	public boolean start(){
		Consumer c = new DefaultConsumer(channel){
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException{
				String message = new String(body, "UTF-8");
	            System.out.println("Customer Received '" + message + "'");
	            //TODO 依据masterSession的状态路由逻辑
	            //正常启动时状态应为working
	            //接收到每一条消息时，先检查session状态
	            //发现masterSession状态为preparing时进入queue清理进程（queue不用unbind，因为session状态为preparing时，新消息会直接入数据库）
			}
		};
		try {
			channel.basicConsume(queueName,true, c);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		this.working = true;
		return true;
	}
	
	public boolean isWorking() {
		return this.working;
	}
	
	//自我销毁
	public void destory() throws IOException {
		this.channel.abort();
		this.masterSession.endConsume();
	}
	
	//TODO 向自己的队列发送结束标志，进入清理进程并读到结束标志的receiver会自我销毁
	public void addEndPoint() {
		
	}
	
}
