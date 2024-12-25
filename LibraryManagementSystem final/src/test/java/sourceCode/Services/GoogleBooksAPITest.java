package sourceCode.Services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import sourceCode.Models.Book;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class GoogleBooksAPITest {

    @Test
    public void testGetBookValidQuery() {
        String query = "Clean Code"; // Truy vấn sách "Clean Code" từ Google Books API
        try {
            JsonArray results = GoogleBooksAPI.getBook(query);

            assertNotNull(results, "Results should not be null");
            assertTrue(results.size() > 0, "Results should contain at least one book");
        } catch (IOException | InterruptedException e) {
            fail("API call failed: " + e.getMessage());
        }
    }

    @Test
    public void testGetBookInvalidQuery() {
        String query = "";
        try {
            JsonArray results = GoogleBooksAPI.getBook(query);
            assertNull(results, "Results should be null or empty for invalid query");
        } catch (IOException | InterruptedException e) {
            assertTrue(e.getMessage().contains("URL errors or can't send request"));
        }
    }

    @Test
    public void testCreateBookFromJson() {
        String json = """
                {
                  "id": "fakeISBN",
                  "volumeInfo": {
                    "title": "A Book",
                    "authors": ["NCMH"],
                    "categories": ["Romance"],
                    "publisher": "UET",
                    "publishedDate": "2000-01-01",
                    "language": "en",
                    "pageCount": 300,
                    "imageLinks": {
                      "thumbnail": "http://test.com/example.jpg"
                    },
                    "description": "This is a book by ncmh."
                  }
                }
                """;

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        Book book = GoogleBooksAPI.createBookFromJson(jsonObject);
        assertNotNull(book, "Book should not be null");
        assertEquals("fakeISBN", book.getISBN(), "ISBN should match");
        assertEquals("A Book", book.getTitle(), "Title should match");
        assertEquals("NCMH", book.getAuthor(), "Author should match");
        assertEquals("Romance", book.getGenre(), "Genre should match");
        assertEquals("UET", book.getPublisher(), "Publisher should match");
        assertEquals("2000-01-01", book.getPublicationDate(), "Publication date should match");
        assertEquals("en", book.getLanguage(), "Language should match");
        assertEquals(300, book.getPageNumber(), "Page count should match");
        assertEquals("http://test.com/example.jpg", book.getImageUrl(), "Image URL should match");
        assertEquals("This is a book by ncmh.", book.getDescription(), "Description should match");
    }
}