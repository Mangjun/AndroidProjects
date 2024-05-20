package hgcq.model.service;


import java.util.List;

import hgcq.model.dto.MemberDTO;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MemberService {

    @POST("/member/join")
    Call<ResponseBody> joinMember(@Body MemberDTO memberDTO);

    @POST("/member/login")
    Call<ResponseBody> loginMember(@Body MemberDTO memberDTO);

    @POST("/member/update")
    Call<ResponseBody> updateMember(@Body MemberDTO memberDTO);

    @POST("/member/logout")
    Call<ResponseBody> logoutMember(@Body MemberDTO memberDTO);

    @POST("/member/friend/add")
    Call<ResponseBody> addFriend(@Body MemberDTO memberDTO);

    @POST("/member/friend/delete")
    Call<ResponseBody> deleteFriend(@Body MemberDTO memberDTO);

    @GET("/member/friend/list")
    Call<List<MemberDTO>> friendList();





}
