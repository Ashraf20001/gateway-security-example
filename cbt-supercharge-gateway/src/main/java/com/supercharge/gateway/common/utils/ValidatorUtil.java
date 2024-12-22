//package com.supercharge.gateway.common.utils;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//import javax.validation.Validation;
//import javax.validation.Validator;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.validation.BeanPropertyBindingResult;
//import org.springframework.validation.Errors;
//import org.springframework.validation.ObjectError;
//import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
//
//import com.cbt.supercharge.config.common.utils.DomainUtils;
//import com.cbt.supercharge.exception.core.ApplicationException;
//import com.cbt.supercharge.exception.core.codes.ErrorCodes;
//import com.cbt.supercharge.transfer.objects.entity.BaseValidator;
//import com.cbt.supercharge.transfer.objects.entity.Institution;
//import com.cbt.supercharge.utils.core.ApplicationUtils;
//
///**
// * @author CBT
// */
//
//@Component
//public class ValidatorUtil {
//
//	private static final Validator javaxValidator = Validation.buildDefaultValidatorFactory().getValidator();
//	private static final SpringValidatorAdapter validator = new SpringValidatorAdapter(javaxValidator);
//	@Autowired
//	private DomainUtils domainUtils;
//
//	public ValidatorUtil(DomainUtils domainUtils) {
//		this.domainUtils = domainUtils;
//	}
//
//	private List<String> buildErrorLogs(Errors errors) {
//		List<String> errorList = new ArrayList<>();
//		for (ObjectError error : errors.getAllErrors()) {
//			errorList.add(error.getDefaultMessage());
//		}
//		return errorList;
//	}
//
//	public void userDomainValidator(String[] domain, List<String> domainList) throws ApplicationException {
//		Boolean isAllowed = domainUtils.isDomainAllowed(domain[1], domainList);
//		if (Boolean.FALSE.equals(isAllowed)) {
//			throw new ApplicationException(ErrorCodes.DOMAIN_VALIDATION);
//		}
//	}
//
//	public void userLimitValidator(Institution institution, Long userCount) throws ApplicationException {
//		if (ApplicationUtils.isValidateObject(institution) && ApplicationUtils.isValidLong(userCount)
//				&& userCount.intValue() >= institution.getUsersAllowed()) {
//			throw new ApplicationException(ErrorCodes.USER_LIMIT_REACHED);
//		}
//	}
//
//	public List<String> validate(BaseValidator entry) {
//		Errors errors = new BeanPropertyBindingResult(entry, entry.getClass().getName());
//		validator.validate(entry, errors);
//		if (errors.getAllErrors().isEmpty()) {
//			return Collections.emptyList();
//		}
//		return buildErrorLogs(errors);
//	}
//}
