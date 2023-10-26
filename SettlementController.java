package jp.co.internous.oasis.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import jp.co.internous.oasis.model.domain.MstDestination;
import jp.co.internous.oasis.model.mapper.MstDestinationMapper;
import jp.co.internous.oasis.model.mapper.TblCartMapper;
import jp.co.internous.oasis.model.mapper.TblPurchaseHistoryMapper;
import jp.co.internous.oasis.model.session.LoginSession;

/**
 * 決済に関する処理を行うコントローラー
 * @author インターノウス
 *
 */
@Controller
@RequestMapping("/oasis/settlement")
public class SettlementController {
	
	/*
	 * フィールド定義
	 */
	@Autowired
	private LoginSession loginSession;
	
	@Autowired
	private MstDestinationMapper destinationMapper;
	
	@Autowired
	private TblPurchaseHistoryMapper purchaseHistoryMapper;
	
	@Autowired
	private TblCartMapper cartMapper;
	
	
	private Gson gson = new Gson();
	
	//--------------------------------------------------
	/**
	 * 宛先選択・決済画面を初期表示する。
	 * @param m 画面表示用オブジェクト
	 * @return 宛先選択・決済画面
	 */
	@RequestMapping("/")
	public String index(Model m) {
		
		int userId = loginSession.getUserId();
		
		List<MstDestination> destinations = destinationMapper.findByUserId(userId);
		
		m.addAttribute("destinations", destinations);
		//
		m.addAttribute("loginSession", loginSession);
		
		return "settlement";
	}
	
	//--------------------------------------------------
	/**
	 * 決済処理を行う
	 * @param destinationId 宛先情報id
	 * @return true:決済処理成功、false:決済処理失敗
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/complete")
	@ResponseBody
	public boolean complete(@RequestBody String destinationId) { 
		
		//desitonationId取得
		Map<String, String> map = gson.fromJson(destinationId, Map.class);
		int id = Integer.parseInt(map.get("destinationId"));
		
		int userId = loginSession.getUserId();
		
		//決済情報を入れる insertCount更新
		int insertCount = purchaseHistoryMapper.insert(id, userId);
		
		//deleteCountを0にセット
		int deleteCount = 0;
		
		//決済情報を入れたのを確認したらユーザー情報に紐づいたカート情報を削除 deleteCount更新
		if (insertCount > 0) {
			deleteCount = cartMapper.deleteByUserId(userId);
		}
		
		return deleteCount == insertCount;
		
	}
}
