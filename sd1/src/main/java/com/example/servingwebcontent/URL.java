package com.example.servingwebcontent;

import org.jsoup.Jsoup;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class URL implements java.io.Serializable {
    String title;
    String url;
    String citation;

    public URL() {
    }

    /**
     * Cria um novo URL com o titulo, url e a citação da página
     * 
     * @param url para criação do objeto URL
     */
    public URL(String url) {
        this.url = url;
        
        getTitleAndCitation();
    }

    /**
     * Obtém o titulo e a citação da página
     */
    void getTitleAndCitation() {
        try {
            Document doc = Jsoup.connect(url).get();
            this.title = doc.title();
            String texto = doc.body().text();
            if (texto != null && texto.length() > 10) {
                this.citation = texto.substring(0, 10) + "...";
            } else {
                this.citation = "Citação não disponível";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getCitation() {
        return citation;
    }

    @Override
    public String toString() {
        return "URL [title=" + title + ", url=" + url + ", citation=" + citation + "]";
    }

}
