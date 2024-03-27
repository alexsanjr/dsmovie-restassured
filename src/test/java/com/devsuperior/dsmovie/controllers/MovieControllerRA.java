package com.devsuperior.dsmovie.controllers;

import com.devsuperior.dsmovie.tests.TokenUtil;
import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

public class MovieControllerRA {

	private String clientUsername, clientPassword, adminUsername, adminPassword;
	private String clientToken, adminToken, invalidToken;
	private Long existingMovieId, nonExistingMovieId, dependentMovieId;

	private Map<String, Object> postMovieInstance;

	@BeforeEach
	void setUp() throws JSONException {
		adminUsername = "maria@gmail.com";
		adminPassword = "123456";
		clientUsername = "alex@gmail.com";
		clientPassword = "123456";

		adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		invalidToken = adminToken + "xpto";

		existingMovieId = 1L;
		nonExistingMovieId = 100L;

		baseURI = "http://localhost:8080";

		postMovieInstance = new HashMap<>();
		postMovieInstance.put("title", "Wakanda Forever");
		postMovieInstance.put("score", 4.5);
		postMovieInstance.put("count", 5);
		postMovieInstance.put("image", "https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg");

	}

	@Test
	public void findAllShouldReturnOkWhenMovieNoArgumentsGiven() {
		given()
				.get("/movies")
				.then()
				.statusCode(200)
				.body("content.id[0]", is(1))
				.body("content.title[0]", equalTo("The Witcher"))
				.body("content.score[0]", is(4.5F))
				.body("content.count[0]", is(2))
				.body("content.image[0]", equalTo("https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg"));
	}
	
	@Test
	public void findAllShouldReturnPagedMoviesWhenMovieTitleParamIsNotEmpty() {
		given()
				.get("movies?page=0")
				.then()
				.statusCode(200)
				.body("content.title", hasItems("The Witcher", "Thor"));
	}
	
	@Test
	public void findByIdShouldReturnMovieWhenIdExists() {
		given()
				.get("/movies/{id}", existingMovieId)
				.then()
				.statusCode(200)
				.body("id", is(1))
				.body("title", equalTo("The Witcher"))
				.body("score", is(4.5F))
				.body("count", is(2))
				.body("image", equalTo("https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg"));
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() {
		given()
				.get("/movies/{id}", nonExistingMovieId)
				.then()
				.statusCode(404)
				.body("error", equalTo("Recurso não encontrado"));
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndBlankTitle() throws JSONException {
		postMovieInstance.put("title", "");
		JSONObject newMovie =  new JSONObject(postMovieInstance);

		given()
				.header("Content-type", "application/json")
				.header("Authorization", "Bearer " + adminToken)
				.body(newMovie)
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.when()
				.post("/movies")
				.then()
				.statusCode(422)
				.body("errors.message[0]", equalTo("Title must be between 5 and 80 characters"));
	}
	
	@Test
	public void insertShouldReturnForbiddenWhenClientLogged() throws Exception {
		JSONObject newMovie =  new JSONObject(postMovieInstance);

		given()
				.header("Content-type", "application/json")
				.header("Authorization", "Bearer " + clientToken)
				.body(newMovie)
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.when()
				.post("/movies")
				.then()
				.statusCode(403);

	}
	
	@Test
	public void insertShouldReturnUnauthorizedWhenInvalidToken() throws Exception {
		JSONObject newMovie =  new JSONObject(postMovieInstance);

		given()
				.header("Content-type", "application/json")
				.header("Authorization", "Bearer " + invalidToken)
				.body(newMovie)
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.when()
				.post("/movies")
				.then()
				.statusCode(401);
	}
}
