package pf.cyj.sys.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {  //Lettuce  비동기/반응형 지원
        return new LettuceConnectionFactory(host, port);
    }
    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

     @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        template.setKeySerializer(new StringRedisSerializer());

        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(valueSerializer);

        return template;
    }

    /**
     * Redis 캐시용 ObjectMapper - GenericJackson2JsonRedisSerializer() 기본 생성자가 내부에서 새로
     * 만드는 ObjectMapper 에는 JavaTimeModule 이 등록돼 있지 않아서(Spring 웹 계층이 쓰는 ObjectMapper와는
     * 완전히 별개), LocalDateTime 필드(DmnPdtRsp.predictedAt 등)가 있는 값을 캐싱하면
     * "Java 8 date/time type not supported" SerializationException 이 난다.
     * 여기서 직접 ObjectMapper를 만들어 JavaTimeModule을 등록하고, activateDefaultTyping은
     * GenericJackson2JsonRedisSerializer() 기본 생성자가 하던 것과 동일하게 맞춰서(캐시에 타입 정보를
     * 같이 저장) 읽어올 때 List<DmnPdtRsp> 같은 원래 타입으로 정확히 역직렬화되게 한다.
     */
    private ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        return mapper;
    }
}