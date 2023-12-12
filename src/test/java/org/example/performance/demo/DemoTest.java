package org.example.performance.demo;

import cn.hutool.core.util.XmlUtil;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import java.math.BigDecimal;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 随意测试
 * @date 2023/12/8 09:17:35
 */
public class DemoTest {
    @Test
    public void test1() {
        BigDecimal bigDecimal = BigDecimal.valueOf(0.00);
        bigDecimal.add(BigDecimal.valueOf(1.74));
        System.out.println(bigDecimal);
        System.out.println(bigDecimal.add(BigDecimal.valueOf(1.74)));
    }

    @Test
    public void test2() {
        Document document = XmlUtil.readXML("host/hosts.xml");
        NodeList nodeList = (NodeList) XmlUtil.getByXPath("//property[@name='hosts']/list/value", document, XPathConstants.NODESET);
        // 遍历所有的 value 元素
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String value = node.getTextContent().trim();
        }

    }
}
