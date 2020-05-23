package br.com.alura.estoque.retrofit;

import org.jetbrains.annotations.NotNull;

import br.com.alura.estoque.retrofit.service.ProdutoService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EstoqueRetrofit {

    private static final String URL_BASE = "http://172.29.186.193:8080/";
    private final ProdutoService produtoService;

    public EstoqueRetrofit() {

        OkHttpClient client = configurarClient();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASE)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        produtoService = retrofit.create(ProdutoService.class);
    }

    @NotNull
    private OkHttpClient configurarClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
    }

    public ProdutoService getProdutoService() {
        return produtoService;
    }
}
