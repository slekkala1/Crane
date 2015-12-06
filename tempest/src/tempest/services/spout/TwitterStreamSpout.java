package tempest.services.spout;

import org.scribe.builder.*;
import org.scribe.builder.api.*;
import org.scribe.model.*;
import org.scribe.oauth.*;

import tempest.interfaces.BaseSpout;
import tempest.protos.Command;
import tempest.services.Tuple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by swapnalekkala on 11/28/15.
 */
public class TwitterStreamSpout implements BaseSpout{
    private static final String STREAM_URI = "https://stream.twitter.com/1.1/statuses/filter.json";
    private static final String CONSUMER_KEY = "pGH3y2FJuPuyVPUlKlM40hoER";
    private static final String CONSUMER_SECRET = "gvFwRBpBe0GuOkXdBac2ymrr1K7L8VGjDxQuH1dtNKgXATJP9P";
    private static final String ACCESS_TOKEN = "47723899-x8Lzzn92VmpiGWu2WXuorVG2ArBbDZyvNIQP6C8eS";
    private static final String ACCESS_TOKEN_SECRET = "5JmQsqm5h8jx3HbDk1dX8WnTC1i4wwD6wudyGsRj9278O";
    
    LinkedBlockingQueue<Tuple> queue;

    public TwitterStreamSpout(LinkedBlockingQueue<Tuple> queue) {
        this.queue = queue;
    }

    public static final tempest.protos.Command.Spout.SpoutType type = Command.Spout.SpoutType.TWITTERSPOUT;

    public tempest.protos.Command.Spout.SpoutType getType() {
        return type;
    }

    public Runnable retrieveTuples(){
    	return new Runnable() {
            @Override
            public void run() {
            	try{
            		System.out.println("Starting Twitter public stream consumer thread.");

            		// Enter your consumer key and secret below
            		OAuthService service = new ServiceBuilder()
                    	.provider(TwitterApi.class)
                    	.apiKey(CONSUMER_KEY)
                    	.apiSecret(CONSUMER_SECRET)
                    	.build();

            		// Set your access token
            		Token accessToken = new Token(ACCESS_TOKEN, ACCESS_TOKEN_SECRET);

            		// Let's generate the request
            		System.out.println("Connecting to Twitter Public Stream");
            		OAuthRequest request = new OAuthRequest(Verb.POST, STREAM_URI);
            		request.addHeader("version", "HTTP/1.1");
            		request.addHeader("host", "stream.twitter.com");
            		request.setConnectionKeepAlive(true);
            		request.addHeader("user-agent", "Twitter Stream Reader");
            		request.addBodyParameter("track", "java,heroku,twitter"); // Set keywords you'd like to track here
            		service.signRequest(accessToken, request);
            		Response response = request.send();

	            // Create a reader to read Twitter's stream
	            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getStream()));

	            String line;
	            while ((line = reader.readLine()) != null) {
	            	try {
	            		List<String> s = Arrays.asList(line.split(","));
	            		Tuple t = new Tuple(s);
	            		queue.put(t);
	            	} catch (InterruptedException e) {
	            		e.printStackTrace();
	            	}
	            }
            	} catch (IOException ioe){
            		ioe.printStackTrace();
            	}
            }
    	};
    }
}
