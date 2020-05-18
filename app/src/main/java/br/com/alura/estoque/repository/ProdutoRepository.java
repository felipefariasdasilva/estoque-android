package br.com.alura.estoque.repository;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class ProdutoRepository {

    private final ProdutoDAO dao;
    private ProdutoService service;

    public ProdutoRepository(ProdutoDAO dao) {
        this.dao = dao;
        service = new EstoqueRetrofit().getProdutoService();
    }

    public void buscaProdutos(DadosCarregadosCallback<List<Produto>> callback) {
        buscarProdutosInternos(callback);
    }

    private void buscarProdutosInternos(DadosCarregadosCallback<List<Produto>> callback) {
        new BaseAsyncTask<>(dao::buscaTodos,
                resultado -> {
                    callback.quandoSucesso(resultado);
                    buscarProdutosNaAPI(callback);
                }).execute();
    }

    private void buscarProdutosNaAPI(DadosCarregadosCallback<List<Produto>> callback) {
        Call<List<Produto>> call = service.buscaTodos();

        call.enqueue(new Callback<List<Produto>>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<List<Produto>> call, Response<List<Produto>> response) {
                if(response.isSuccessful()){
                    List<Produto> produtosNovos = response.body();
                    if(produtosNovos != null){
                        atualizarInternamente(produtosNovos, callback);
                    }
                }else {
                    callback.quandoFalha("Resposta não sucedida");
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<List<Produto>> call, Throwable t) {
                callback.quandoFalha("Falha de comunicação: " + t.getMessage());
            }
        });
    }

    private void atualizarInternamente(List<Produto> produtosNovos, DadosCarregadosCallback<List<Produto>> callback) {
        new BaseAsyncTask<>(() -> {
            dao.salva(produtosNovos);
            return dao.buscaTodos();
        }, callback::quandoSucesso )
                .execute();
    }

    public void salva(Produto produto, DadosCarregadosCallback<Produto> callback) {
        salvarNaApi(produto, callback);

    }

    private void salvarNaApi(Produto produto, DadosCarregadosCallback<Produto> callback) {
        Call<Produto> call = service.salva(produto);
        call.enqueue(new Callback<Produto>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<Produto> call, Response<Produto> response) {
                if (response.isSuccessful()) {
                    Produto produtoSalvo = response.body();
                    if(produtoSalvo != null){
                        salvarInternamente(produtoSalvo, callback);
                    }
                }else{
                    callback.quandoFalha("Resposta não sucedida");
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<Produto> call, Throwable t) {
                callback.quandoFalha("Falha de comunicação: " + t.getMessage());
            }
        });
    }

    private void salvarInternamente(Produto produtoSalvo, DadosCarregadosCallback<Produto> callback) {
        new BaseAsyncTask<>(() -> {
            long id = dao.salva(produtoSalvo);
            return dao.buscaProduto(id);
        }, callback::quandoSucesso)
                .execute();
    }


    public interface DadosCarregadosCallback<T> {
        void quandoSucesso(T resultado);
        void quandoFalha(String erro);
    }
}
