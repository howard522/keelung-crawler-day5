package com.example.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            Elements links = ul.select("li a[href]");

            for (Element link : links) {
                String detailUrl = link.absUrl("href");
                Document detailDoc = Jsoup.connect(detailUrl)
                                          .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                                          .timeout(10000)
                                          .get();

                Sight s = new Sight();
                // 名稱
                Element metaName = detailDoc.selectFirst("meta[itemprop=name]");
                s.setSightName(metaName != null
                    ? metaName.attr("content").trim()
                    : detailDoc.selectFirst("h1").text().trim());
                s.setZone(zoneName);

                // 類別
                Element catEl = detailDoc.selectFirst(
                  "span.point_pc + span[property=rdfs:label] strong");
                s.setCategory(catEl != null ? catEl.text().trim() : "");

                // 圖片
                Element metaImg = detailDoc.selectFirst("meta[property=og:image]");
                String photoUrl = metaImg != null
                    ? metaImg.attr("content")
                    : detailDoc.selectFirst("link[rel=image_src]").attr("href");
                s.setPhotoURL(photoUrl);

                // 地址
                Element addr = detailDoc.selectFirst(
                  "#point_data div.address span[property=vcard:street-address]");
                s.setAddress(addr != null ? addr.text().trim() : "");

                // 描述
                Element desc = detailDoc.selectFirst("meta[itemprop=description]");
                s.setDescription(desc != null ? desc.attr("content").trim() : "");

                list.add(s);
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
