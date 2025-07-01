package com.yowyob.dev.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Configuration
@EnableR2dbcRepositories
public class R2dbcConfig extends AbstractR2dbcConfiguration {

    private final ConnectionFactory connectionFactory;

    public R2dbcConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public ConnectionFactory connectionFactory() {
        return this.connectionFactory;
    }

    @Bean
    @Override
    public R2dbcCustomConversions r2dbcCustomConversions() {
        return new R2dbcCustomConversions(getStoreConversions(), Arrays.asList(
                new UuidToByteArrayConverter(),
                new ByteArrayToUuidConverter()
        ));
    }

    /**
     * Convertit UUID vers byte[] pour l'écriture en base (BINARY(16))
     */
    @WritingConverter
    public static class UuidToByteArrayConverter implements Converter<UUID, byte[]> {
        @Override
        public byte[] convert(UUID source) {
            if (source == null) {
                return null;
            }

            ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.putLong(source.getMostSignificantBits());
            buffer.putLong(source.getLeastSignificantBits());
            return buffer.array();
        }
    }

    /**
     * Convertit byte[] vers UUID pour la lecture depuis la base (BINARY(16))
     */
    @ReadingConverter
    public static class ByteArrayToUuidConverter implements Converter<byte[], UUID> {
        @Override
        public UUID convert(byte[] source) {
            if (source == null || source.length != 16) {
                return null;
            }

            ByteBuffer buffer = ByteBuffer.wrap(source);
            long mostSignificantBits = buffer.getLong();
            long leastSignificantBits = buffer.getLong();
            return new UUID(mostSignificantBits, leastSignificantBits);
        }
    }
}