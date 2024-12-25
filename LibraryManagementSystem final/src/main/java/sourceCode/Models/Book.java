package sourceCode.Models;

public class Book {

    private String ISBN;
    private String title;
    private String author;
    private String genre;
    private String publisher;
    private String publicationDate;
    private String language;
    private int pageNumber;
    private String imageUrl;
    private String description;
    private int quantity;

    public Book() {
        ISBN = "";
        title = "";
        author = "";
        genre = "";
        publisher = "";
        publicationDate = "";
        language = "";
        pageNumber = 0;
        imageUrl = null ;
        description = "";
        quantity = 0;
    }

    public Book(String ISBN, String title, String author, String genre, String publisher,
            String publicationDate, String language, int pageNumber, String imageUrl,
            String description, int quantity) {
        this.ISBN = ISBN;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publisher = publisher;
        this.publicationDate = publicationDate;
        this.language = language;
        this.pageNumber = pageNumber;
        this.imageUrl = imageUrl;
        this.description = description;
        this.quantity = quantity;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
