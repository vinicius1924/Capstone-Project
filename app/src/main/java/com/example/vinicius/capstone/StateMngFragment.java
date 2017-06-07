package com.example.vinicius.capstone;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.HashMap;

/**
 * Created by vinicius on 08/12/16.
 */
/**
 * Armazena e administra os objetos que devem ser preservados
 * durante mudanças de configuração.
 * É instanciado somente uma vez e utiliza um
 * <code>HashMap</code> para salvar os objetos e suas
 * chaves de referência.
 */
public class StateMngFragment extends Fragment
{
	private HashMap<String, Object> mData = new HashMap<>();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// Garante que o Fragmento será preservado
		// durante mudanças de configuração
		setRetainInstance(true);
	}

	/**
	 * Insere objetos no hashmap
	 *
	 * @param key Chave de referência
	 * @param obj Objeto a ser salvo
	 */
	public void put(String key, Object obj)
	{
		mData.put(key, obj);
	}

	/**
	 * Insere objeto utilizando o nome da classe como referência
	 *
	 * @param object Objeto a ser salvo
	 */
	public void put(Object object)
	{
		put(object.getClass().getName(), object);
	}

	/**
	 * Recupera objeto salvo no hashmap
	 *
	 * @param key Chave de referência
	 * @param <T> Classe
	 * @return Objeto salvo
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String key)
	{
		return (T) mData.get(key);
	}
}
