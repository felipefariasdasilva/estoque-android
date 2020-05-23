package br.com.alura.estoque.repository;

import android.content.Context;

import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.EstoqueDatabase;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.callback.CallComRetorno;
import br.com.alura.estoque.retrofit.callback.CallbackSemRetorno;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;

public class ProdutoRepository {

    private final ProdutoDAO dao;
    private ProdutoService service;

    public ProdutoRepository(Context context) {
        EstoqueDatabase db = EstoqueDatabase.getInstance(context);
        dao = db.getProdutoDAO();
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

        call.enqueue(new CallComRetorno<>(new CallComRetorno.RespostaCallback<List<Produto>>() {
            @Override
            public void quandoSucesso(List<Produto> produtosNovos) {
                atualizarInternamente(produtosNovos, callback);
            }

            @Override
            public void quandoFalha(String erro) {
                callback.quandoFalha(erro);
            }
        }));
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
        call.enqueue(new CallComRetorno<>(new CallComRetorno.RespostaCallback<Produto>() {
            @Override
            public void quandoSucesso(Produto produtoSalvo) {
                salvarInternamente(produtoSalvo, callback);
            }

            @Override
            public void quandoFalha(String erro) {
                callback.quandoFalha(erro);
            }
        }));
    }

    private void salvarInternamente(Produto produtoSalvo, DadosCarregadosCallback<Produto> callback) {
        new BaseAsyncTask<>(() -> {
            long id = dao.salva(produtoSalvo);
            return dao.buscaProduto(id);
        }, callback::quandoSucesso)
                .execute();
    }

    public void edita(Produto produto, DadosCarregadosCallback<Produto> callback) {

        Call<Produto> call  = service.edita(produto.getId(), produto);
        editaNaApi(produto, callback, call);
    }

    private void editaNaApi(Produto produto, DadosCarregadosCallback<Produto> callback, Call<Produto> call) {
        call.enqueue(new CallComRetorno<>(new CallComRetorno.RespostaCallback<Produto>() {
            @Override
            public void quandoSucesso(Produto resultado) {
                editaInternamente(produto, callback);
            }

            @Override
            public void quandoFalha(String erro) {
                callback.quandoFalha(erro);
            }
        }));
    }

    private void editaInternamente(Produto produto, DadosCarregadosCallback<Produto> callback) {
        new BaseAsyncTask<>(() -> {
            dao.atualiza(produto);
            return produto;
        }, callback::quandoSucesso)
                .execute();
    }

    public void remove(Produto produto, DadosCarregadosCallback<Void> callback) {
        removerNaApi(produto, callback);
    }

    private void removerNaApi(Produto produto, DadosCarregadosCallback<Void> callback) {
        Call<Void> call = service.remove(produto.getId());
        call.enqueue(new CallbackSemRetorno(new CallbackSemRetorno.RespostaCallback() {
            @Override
            public void quandoSucesso() {
                removerInternamente(produto, callback);
            }

            @Override
            public void quandoFalha(String erro) {
                callback.quandoFalha(erro);
            }
        }));
    }

    private void removerInternamente(Produto produto, DadosCarregadosCallback<Void> callback) {
        new BaseAsyncTask<>(() -> {
            dao.remove(produto);
            return null;
        }, callback::quandoSucesso)
                .execute();
    }

    public interface DadosCarregadosCallback<T> {
        void quandoSucesso(T resultado);
        void quandoFalha(String erro);
    }
}
