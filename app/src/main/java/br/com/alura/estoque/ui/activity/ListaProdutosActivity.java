package br.com.alura.estoque.ui.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import br.com.alura.estoque.R;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.repository.ProdutoRepository;
import br.com.alura.estoque.ui.dialog.EditaProdutoDialog;
import br.com.alura.estoque.ui.dialog.SalvaProdutoDialog;
import br.com.alura.estoque.ui.recyclerview.adapter.ListaProdutosAdapter;

public class ListaProdutosActivity extends AppCompatActivity {

    private static final String TITULO_APPBAR = "Lista de produtos";
    private static final String MENSAGEM_ERRO_BUSCA_PRODUTOS = "Não foi possível carregar os produtos novos";
    private static final String MENSAGEM_ERRO_REMOCAO = "Nao foi possivel remover o produto";
    private static final String MENSAGEM_ERRO_SALVA = "Não foi possível salvar o produto";
    private static final String MENSAGEM_ERRO_ATUALIZA = "Nao foi possivel carregar o produto";
    private ListaProdutosAdapter adapter;
    private ProdutoRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_produtos);
        setTitle(TITULO_APPBAR);

        configuraListaProdutos();
        configuraFabSalvaProduto();

        repository = new ProdutoRepository(this);
        buscarProduto();
    }

    private void buscarProduto() {
        repository.buscaProdutos(new ProdutoRepository.DadosCarregadosCallback<List<Produto>>() {
            @Override
            public void quandoSucesso(List<Produto> produtosNovos) {
                adapter.atualiza(produtosNovos);
            }

            @Override
            public void quandoFalha(String erro) {
                mostrarErro(MENSAGEM_ERRO_BUSCA_PRODUTOS);
            }
        });
    }

    private void mostrarErro(String mensagem) {
        Toast.makeText(
                this,
                mensagem, Toast.LENGTH_SHORT).show();
    }

    private void configuraListaProdutos() {
        RecyclerView listaProdutos = findViewById(R.id.activity_lista_produtos_lista);
        adapter = new ListaProdutosAdapter(this, this::abreFormularioEditaProduto);
        listaProdutos.setAdapter(adapter);

        adapter.setOnItemClickRemoveContextMenuListener(this::remover);
    }

    private void remover(int posicao, Produto produtoEscolhido) {
        repository.remove(produtoEscolhido,
                new ProdutoRepository.DadosCarregadosCallback<Void>() {
                    @Override
                    public void quandoSucesso(Void resultado) {
                        adapter.remove(posicao);
                    }

                    @Override
                    public void quandoFalha(String erro) {
                        mostrarErro(MENSAGEM_ERRO_REMOCAO);
                    }
                });
    }

    private void configuraFabSalvaProduto() {
        FloatingActionButton fabAdicionaProduto = findViewById(R.id.activity_lista_produtos_fab_adiciona_produto);
        fabAdicionaProduto.setOnClickListener(v -> abreFormularioSalvaProduto());
    }

    private void abreFormularioSalvaProduto() {
        new SalvaProdutoDialog(this, this::salvar).mostra();
    }

    private void salvar(Produto produtoCriado) {
        repository.salva(produtoCriado, new ProdutoRepository.DadosCarregadosCallback<Produto>() {
            @Override
            public void quandoSucesso(Produto produtoSalvo) {
                adapter.adiciona(produtoSalvo);
            }

            @Override
            public void quandoFalha(String erro) {
                mostrarErro(MENSAGEM_ERRO_SALVA);
            }
        });
    }

    private void abreFormularioEditaProduto(int posicao, Produto produto) {
        new EditaProdutoDialog(this, produto,
                produtoCriado -> editar(posicao, produtoCriado))
                .mostra();
    }

    private void editar(int posicao, Produto produtoCriado) {
        repository.edita(produtoCriado,
                new ProdutoRepository.DadosCarregadosCallback<Produto>() {
            @Override
            public void quandoSucesso(Produto produtoEditado) {
                adapter.edita(posicao, produtoEditado);
            }

            @Override
            public void quandoFalha(String erro) {
                mostrarErro(MENSAGEM_ERRO_ATUALIZA);

            }
        });
    }

}
