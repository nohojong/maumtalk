// src/api/auth.js
import apiClient from "./index";

export const signup = async (email, password) => {
  return await apiClient.post("/auth/signup", { email, password });
};

export const login = async (email, password) => {
  return await apiClient.post("/auth/login", { email, password });
};

export const logout = async () => {
  return await apiClient.post("/auth/logout");
};

export const refresh = async () => {
  return await apiClient.post("/auth/refresh");
};

export const checkAuthStatus = async () => {
  return await apiClient.get("/auth/me");
};
