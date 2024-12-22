package com.supercharge.gateway.common.filter.master;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.bson.BsonDateTime;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.transfter.objects.core.dto.FilterOrSortingVo;
import com.supercharge.gateway.common.base.dao.DataTypeConvertorGateway;
import com.supercharge.gateway.common.base.dao.IGatewayMasterMongoFilter;

@Service
@Qualifier("GBWMongoFilter")
public class BetweenMongoFilterGateway implements IGatewayMasterMongoFilter {

	/**
	 * The data type convertor.
	 */
	@Autowired
	private DataTypeConvertorGateway dataTypeConvertor;

	/**
	 * @param filterVo
	 */
	@Override
	public Document getFilterPredicate(FilterOrSortingVo filterVo) throws ApplicationException {
		Document betweenFilter = new Document();
		if (ApplicationConstants.FILTER_TYPE_DATE.equals(filterVo.getType())) {
			Comparable comFromDate = dataTypeConvertor.converToRealDataType(filterVo.getValue(), filterVo.getType());
			Comparable comToDate = dataTypeConvertor.converToRealDataType(filterVo.getValue2(), filterVo.getType());

			Date fromDate = parseDate(comFromDate.toString());
			Date toDate = parseDate(comToDate.toString());

			// Convert Date to Instant
			betweenFilter.append(filterVo.getColumnName(),
					new Document(ApplicationConstants.GREATERTHAN_EQUALSTO,
							new Document(ApplicationConstants.$DATE, fromDate))
							.append(ApplicationConstants.LESSERTHAN_EQUALSTO,
									new Document(ApplicationConstants.$DATE, toDate)));

			betweenFilter.append(filterVo.getColumnName(),
					new Document(ApplicationConstants.GREATERTHAN_EQUALSTO, new BsonDateTime(fromDate.getTime()))
							.append(ApplicationConstants.LESSERTHAN_EQUALSTO, new BsonDateTime(toDate.getTime())));
		} else {
			Object value1 = dataTypeConvertor.converToRealDataType(filterVo.getValue(), filterVo.getType());
			Object value2 = dataTypeConvertor.converToRealDataType(filterVo.getValue2(), filterVo.getType());
			betweenFilter.append(filterVo.getColumnName(),
					new Document(ApplicationConstants.GREATERTHAN_EQUALSTO, value1)
							.append(ApplicationConstants.LESSERTHAN_EQUALSTO, value2));
		}
		return betweenFilter;
	}

	/**
	 * @param dateString
	 * @return
	 */
	private static Date parseDate(String dateString) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
			Date dates = dateFormat.parse(dateString);
			Instant instant = dates.toInstant();

			// Format Instant to ISO date string
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
					.withZone(ZoneId.of("UTC"));

			return Date.from(Instant.parse(formatter.format(instant)));
		} catch (ParseException e) {
			throw new RuntimeException("Error parsing date: " + dateString, e);
		}
	}

}
