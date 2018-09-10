package com.github.hicolors.leisure.common.framework.logger;

import brave.Tracer;
import com.github.hicolors.leisure.common.utils.JSONUtils;
import com.github.hicolors.leisure.common.utils.ThreadLocalUtils;
import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.instrument.web.TraceWebFilter;
import org.springframework.core.annotation.Order;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 日志过滤器
 * 打印 access log
 *
 * @author weichao.li (liweichao0102@gmail.com)
 * @date 2018/8/19
 */
@Order(LoggerFilter.ORDER)
public class LoggerFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerFilter.class);

    public static final int ORDER = TraceWebFilter.ORDER + 7;

    @Autowired
    private Tracer tracer;

    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    private PathMatcher pathMatcher = new AntPathMatcher();

    private final List<String> excludePatterns;

    public LoggerFilter(List<String> excludePatterns) {
        this.excludePatterns = excludePatterns;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //request 时间
        Date requestTime = new Date();
        //包装器
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            try {
                //过滤不打印日志的 url
                String uri = this.urlPathHelper.getPathWithinApplication(request);
                boolean skip = false;
                for (String excludePattern : excludePatterns) {
                    if (pathMatcher.match(excludePattern, uri)) {
                        skip = true;
                    }
                }
                if (!skip) {
                    Map<String, String> resultMap = new HashMap<>();

                    /*
                     * request 日志处理
                     */
                    Map<String, String> extraParamMap = Maps.newConcurrentMap();
                    String bodyParam = new String(requestWrapper.getContentAsByteArray(), requestWrapper.getCharacterEncoding());
                    extraParamMap.putAll(ExtraParamUtils.getAll());
                    if (MapUtils.isNotEmpty(requestWrapper.getParameterMap())) {
                        extraParamMap.put(LoggerConst.REQUEST_KEY_FORM_PARAM, JSONUtils.serialize(request.getParameterMap()));
                    }
                    Enumeration<String> requestEntries = request.getHeaderNames();
                    while (requestEntries.hasMoreElements()) {
                        String headerName = requestEntries.nextElement();
                        resultMap.put((LoggerConst.REQUEST_HEADER_PREFIX + headerName).toLowerCase(), requestWrapper.getHeader(headerName));
                    }
                    /*
                     * response 日志处理
                     */
                    String responseData = IOUtils.toString(responseWrapper.getContentInputStream(), responseWrapper.getCharacterEncoding());

                    /*
                     * 日志处理
                     */
                    resultMap.put(LoggerConst.REQUEST_KEY_TRACE_ID, tracer.currentSpan().context().traceIdString());
                    resultMap.put(LoggerConst.REQUEST_KEY_REQUEST_TIME, LoggerConst.DATE_FORMAT.format(requestTime));
                    resultMap.put(LoggerConst.REQUEST_KEY_URL, request.getRequestURL().toString());
                    resultMap.put(LoggerConst.REQUEST_KEY_HTTP_METHOD, request.getMethod());
                    resultMap.put(LoggerConst.REQUEST_KEY_BODY_PARAM, StringUtils.isNotBlank(bodyParam) ? org.springframework.util.StringUtils.trimAllWhitespace(bodyParam) : LoggerConst.VALUE_DEFAULT);
                    resultMap.put(LoggerConst.REQUEST_KEY_EXTRA_PARAM, JSONUtils.serialize(extraParamMap));
                    Date responseDate = new Date();
                    resultMap.put(LoggerConst.RESPONSE_KEY_RESPONSE_TIME, LoggerConst.DATE_FORMAT.format(responseDate));
                    resultMap.put(LoggerConst.RESPONSE_KEY_TAKE_TIME, String.valueOf(responseDate.getTime() - requestTime.getTime()));
                    resultMap.put(LoggerConst.RESPONSE_KEY_HTTP_STATUS, String.valueOf(responseWrapper.getStatus()));
                    resultMap.put(LoggerConst.RESPONSE_KEY_RESPONSE_DATA, StringUtils.isNotBlank(responseData) ? org.springframework.util.StringUtils.trimAllWhitespace(responseData) : LoggerConst.VALUE_DEFAULT);
                    resultMap.put(LoggerConst.RESPONSE_KEY_CONTENT_LENGTH, String.valueOf(responseWrapper.getContentSize()));
                    resultMap.put(LoggerConst.RESPONSE_KEY_CONTENT_TYPE, responseWrapper.getContentType());
                    //组装request和response
                    StringBuilder logResult = generateResultLogger(resultMap);
                    LOGGER.info(logResult.toString());
                }
            } catch (Exception ignored) {
            } finally {
                ThreadLocalUtils.clear();
                responseWrapper.copyBodyToResponse();
            }
        }
    }

    private static StringBuilder generateResultLogger(Map<String, String> resultMap) {
        StringBuilder resultStr = new StringBuilder();

        //遍历request key, 并将相应的值写入StringBuilder中
        resultStr.append("\n");
        for (String requestKey : LoggerConst.REQUEST_KEY_LIST) {
            String requestValue = resultMap.get(requestKey);
            if (StringUtils.isBlank(requestValue)) {
                requestValue = LoggerConst.VALUE_DEFAULT;
            }
            appendKeyValue(resultStr, requestKey, requestValue, LoggerConst.REQUEST_PREFIX);
        }
        resultStr.append("\n");
        //遍历response key, 并将相应的值写入StringBuilder中
        for (String responseKey : LoggerConst.RESPONSE_KEY_LIST) {
            String responseValue = resultMap.get(responseKey);
            if (StringUtils.isBlank(responseValue)) {
                responseValue = LoggerConst.VALUE_DEFAULT;
            }
            appendKeyValue(resultStr, responseKey, responseValue, LoggerConst.RESPONSE_PREFIX);
        }

        return resultStr;
    }

    private static void appendKeyValue(StringBuilder sb, String key, String value, String prefix) {
        sb.append(prefix).append(key).append(LoggerConst.KEY_VALUE_SEPERATOR).append(value).append("\n");
    }
}