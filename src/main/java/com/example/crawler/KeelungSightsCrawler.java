package com.example.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class KeelungSightsCrawler {
    private static final String BASE_URL = "https://www.travelking.com.tw/tourguide/taiwan/keelungcity/";

    public Sight[] getItems(String zoneFilter) throws IOException {
    Document doc = Jsoup.connect(BASE_URL)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .timeout(10000)
                        .get();

    Element guidePoint = doc.getElementById("guide-point");
    Elements headings   = guidePoint.select("h4");
    List<Sight> list    = new ArrayList<>();

    for (Element heading : headings) {
        String zoneName = heading.text().trim();
        if (!zoneName.contains(zoneFilter)) continue;

        Element ul = heading.nextElementSibling();
        if (ul == null || !"ul".equals(ul.tagName())) continue;
        Elements links = ul.select("li a[href]");

        for (Element link : links) {
            String detailUrl = link.absUrl("href");
            try {
                Document detailDoc = Jsoup.connect(detailUrl)
                                          .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                                          .timeout(10000)
                                          .get();

                Sight s = new Sight();

                // 名称
                Element metaName = detailDoc.selectFirst("meta[itemprop=name]");
                if (metaName != null) {
                    s.setSightName(metaName.attr("content").trim());
                } else {
                    Element h1 = detailDoc.selectFirst("h1");
                    s.setSightName(h1 != null ? h1.text().trim() : "");
                }
                s.setZone(zoneName);

                // 类别
                Element catEl = detailDoc.selectFirst(
                  "span.point_pc + span[property=rdfs:label] strong");
                s.setCategory(catEl != null ? catEl.text().trim() : "");

                // 图片 URL
                String photoUrl = "";
                Element metaImg = detailDoc.selectFirst("meta[property=og:image]");
                if (metaImg != null) {
                    photoUrl = metaImg.attr("content");
                } else {
                    Element linkImg = detailDoc.selectFirst("link[rel=image_src]");
                    if (linkImg != null) {
                        photoUrl = linkImg.attr("href");
                    }
                }
                s.setPhotoURL(photoUrl);

                // 地址
                String addr = "";
                Element addrSpan = detailDoc.selectFirst(
                  "#point_data div.address span[property=vcard:street-address]");
                if (addrSpan != null) {
                    addr = addrSpan.text().trim();
                }
                s.setAddress(addr);

                // 描述
                String desc = "";
                Element metaDesc = detailDoc.selectFirst("meta[itemprop=description]");
                if (metaDesc != null) {
                    desc = metaDesc.attr("content").trim();
                }
                s.setDescription(desc);

                list.add(s);
            } catch (Exception e) {
                // 单个景点抓取失败时，打印日志并继续
                System.err.println("Failed to parse detail: " + detailUrl);
                e.printStackTrace();
            }
        }
    }

    return list.toArray(new Sight[0]);
}

    public static void main(String[] args) throws IOException {
        KeelungSightsCrawler crawler = new KeelungSightsCrawler();
        for (Sight s : crawler.getItems("七堵")) {
            System.out.println("------------");
            System.out.println(s);
        }
    }
}
