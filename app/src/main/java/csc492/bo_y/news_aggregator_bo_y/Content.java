package csc492.bo_y.news_aggregator_bo_y;

import android.content.Context;

import java.io.Serializable;

public class Content implements Serializable {
    String author;
    String title;
    String description;
    String url;
    String urlToImage;
    String publishedAt;
    public Content(String author,
            String title,
            String description,
            String url,
            String urlToImage,
            String publishedAt){
        this.author = author;
        this.description = description;
        this.title = title;
        this.url = url;
        this.urlToImage = urlToImage;
        this.publishedAt = publishedAt;


    }

    public String getAuthor(){return author;}
    public String getTitle(){return title;}
    public String getUrl(){return url;}
    public String getUrlToImage(){
        return urlToImage;
    }
    public String getPublishedAt(){return publishedAt;}
    public String getDescription() {
        return description;
    }
}
