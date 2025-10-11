package dev.harrel.jsonschema;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

final class EvalState {
    private final Annotation[] annotations = new Annotation[12];
    private Map<String, Annotation> annotationsMap;
    final URI schemaUri;
    final int annotationsBefore;

    EvalState(URI schemaUri, int annotationsBefore) {
        this.schemaUri = schemaUri;
        this.annotationsBefore = annotationsBefore;
    }

    Annotation getSiblingAnnotation(String keyword) {
        int id = getKeywordId(keyword);
        if (id >= 0) {
            return annotations[id];
        }
        if (annotationsMap == null) {
            return null;
        }
        return annotationsMap.get(keyword);
    }

    void setSiblingAnnotation(String keyword, Annotation annotation) {
        int id = getKeywordId(keyword);
        if (id >= 0) {
            annotations[id] = annotation;
            return;
        }
        if (annotationsMap == null) {
            annotationsMap = new HashMap<>();
        }
        annotationsMap.put(keyword, annotation);
    }

    private static int getKeywordId(String keyword) {
        switch (keyword) {
            case Keyword.TITLE: return 0;
            case Keyword.DESCRIPTION: return 1;
            case Keyword.DEFAULT: return 2;
            case Keyword.PROPERTIES: return 3;
            case Keyword.PATTERN_PROPERTIES: return 4;
            case Keyword.ADDITIONAL_PROPERTIES: return 5;
            case Keyword.UNEVALUATED_PROPERTIES: return 6;
            case Keyword.ITEMS: return 7;
            case Keyword.PREFIX_ITEMS: return 8;
            case Keyword.ADDITIONAL_ITEMS: return 9;
            case Keyword.UNEVALUATED_ITEMS: return 10;
            case Keyword.CONTAINS: return 11;
            default: return -1;
        }
    }
}
