package edu.stanford.bmir.protege.web.server.persistence;

import com.google.common.io.BaseEncoding;
import edu.stanford.bmir.protege.web.shared.auth.Salt;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.util.Locale;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 09/03/16
 */
@WritingConverter
public class SaltWriteConverter implements Converter<Salt, String> {

    @Override
    public String convert(Salt salt) {
        return BaseEncoding.base16().encode(salt.getBytes()).toLowerCase(Locale.ENGLISH);
    }
}