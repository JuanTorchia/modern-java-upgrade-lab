package dev.modernjava.upgrade.example;

import javax.xml.bind.JAXBContext;

final class LegacyXmlBindingAdapter {

    JAXBContext contextFor(Class<?> type) throws Exception {
        return JAXBContext.newInstance(type);
    }
}
