import api from './api'

export function login(username, password) {
    return api.post('/auth/login', { username, password }).then((res) => res.data)
}

export function register({ nome, username, password, confirmPassword }) {
    return api
        .post('/auth/register', { nome, username, password, confirmPassword })
        .then((res) => res.data)
}
