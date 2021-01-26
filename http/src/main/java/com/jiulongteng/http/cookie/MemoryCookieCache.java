/*
 * Copyright (C) 2016 Francisco José Montiel Navarro.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiulongteng.http.cookie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class MemoryCookieCache implements ClearableCookieJar {

    private Set<IdentifiableCookie> cookies;
    SetCookieCacheIterator iterator;

    public MemoryCookieCache() {
        cookies = new HashSet<>();
        iterator = new SetCookieCacheIterator();
    }


    /**
     * All cookies will be added to the collection, already existing cookies will be overwritten by the new ones.
     *
     * @param cookies
     */
    private void updateCookies(Collection<IdentifiableCookie> cookies) {
        this.cookies.addAll(cookies);
    }

    @Override
    public void clear() {
        cookies.clear();
    }

    public Iterator<Cookie> iterator() {
        return iterator;
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        updateCookies(IdentifiableCookie.decorateAll(cookies));
    }

    public void addAll(List<Cookie> cookies){
        this.cookies.addAll(IdentifiableCookie.decorateAll(cookies));
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> validCookies = new ArrayList<>();
        Iterator<Cookie> it = iterator();
        while (it.hasNext()) {
            Cookie currentCookie = it.next();

            if (isCookieExpired(currentCookie)) {
                it.remove();

            } else if (currentCookie.matches(url)) {
                validCookies.add(currentCookie);
            }
        }

        return validCookies;

    }

    private static boolean isCookieExpired(Cookie cookie) {
        return cookie.expiresAt() < System.currentTimeMillis();
    }

    private class SetCookieCacheIterator implements Iterator<Cookie> {

        private Iterator<IdentifiableCookie> iterator;

        public SetCookieCacheIterator() {
            iterator = cookies.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Cookie next() {
            return iterator.next().getCookie();
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }
}
