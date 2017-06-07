package com.example.vinicius.capstone;

import android.support.v4.app.FragmentManager;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by vinicius on 04/12/16.
 */

public class StateMaintainer
{
	protected final String TAG = getClass().getSimpleName();

	private final String mStateMaintenerTag;
	private final WeakReference<FragmentManager> mFragmentManager;
	private StateMngFragment mStateMngFrag;

	/**
	 * Construtor
	 *
	 * @param fragmentManager    repassa uma referência do FragmentManager
	 * @param stateMaintainerTAG a TAG utilizada para inserir o fragmento responsável
	 *                           por manter os objetos "vivos"
	 */
	public StateMaintainer(FragmentManager fragmentManager, String stateMaintainerTAG)
	{
		mFragmentManager = new WeakReference<>(fragmentManager);
		mStateMaintenerTag = stateMaintainerTAG;
	}

	/**
	 * cria o fragmento responsável por armazenar o objetos
	 *
	 * @return true: criou o fragmento e rodou pela primeira vez
	 * false: o objeto já foi criado, portanto é apenas recuperado
	 */
	public boolean firstTimeIn()
	{
		try
		{
			// Recuperando referência
			mStateMngFrag = (StateMngFragment) mFragmentManager.get().findFragmentByTag(mStateMaintenerTag);

			// Criando novo RetainedFragment
			if(mStateMngFrag == null)
			{
				Log.d(TAG, "Criando novo RetainedFragment " + mStateMaintenerTag);
				mStateMngFrag = new StateMngFragment();
				mFragmentManager.get().beginTransaction().add(mStateMngFrag, mStateMaintenerTag).commit();
				return true;
			}
			else
			{
				Log.d(TAG, "Retornando retained fragment existente " + mStateMaintenerTag);
				return false;
			}
		}
		catch(NullPointerException e)
		{
			Log.e(TAG, "StateMaintainer.firstTimeIn() - " + e.toString());
			return false;
		}
	}


	/**
	 * Insere objeto a serem presenrvados durante mudanças de configuração
	 *
	 * @param key TAG de referência para recuperação do objeto
	 * @param obj Objeto a ser mantido
	 */
	public void put(String key, Object obj)
	{
		mStateMngFrag.put(key, obj);
	}

	/**
	 * Insere objeto a serem presenrvados durante mudanças de configuração.
	 * Utiliza a classe do Objeto como referência futura.
	 * Só deve ser utilizado somente uma vez por classe, caso contrário haverá
	 * possíveis conflitos na recuperação dos dados
	 *
	 * @param obj Objeto a ser mantido
	 */
	public void put(Object obj)
	{
		put(obj.getClass().getName(), obj);
	}


	/**
	 * Recupera o objeto salvo
	 *
	 * @param key Chave de referência do obj
	 * @param <T> tipo genérico de retorno
	 * @return Objeto armazenado
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String key)
	{
		return mStateMngFrag.get(key);

	}

	/**
	 * Verifica a existência de um objeto com a chave fornecida
	 *
	 * @param key Chave para verificação
	 * @return true: obj existe
	 * false: obj insexistente
	 */
	public boolean hasKey(String key)
	{
		return mStateMngFrag.get(key) != null;
	}
}
