package pf.cyj.sys.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * DB의 CHAR(1) 'Y'/'N' 컬럼 <-> Java boolean 매핑용 컨버터.
 * 엔티티 필드는 boolean 으로 두고 @Convert(converter = YnConverter.class) 를 붙여 사용한다.
 */
@Converter
public class YnConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        return Boolean.TRUE.equals(attribute) ? "Y" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        return "Y".equalsIgnoreCase(dbData);
    }
}
