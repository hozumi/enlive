;   Copyright (c) Christophe Grand, 2009. All rights reserved.

;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this 
;   distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns net.cgrand.enlive-html.test
  (:use net.cgrand.enlive-html)
  (:require [net.cgrand.xml :as xml])
  (:require [clojure.zip :as z])
  (:use [clojure.contrib.test-is :as test-is :only [set-test is are]]))

;; test utilities
(defn- normalize [x]
  (if (string? x)
    (html-snippet x)
    (html-resource x)))

(defn- same? [& xs]
  (apply = (map normalize xs)))

(defmacro #^{:private true} 
 is-same
 [& forms]
 `(is (same? ~@forms)))

(defn- test-step [expected pred node]
  (= expected (boolean (pred (xml/xml-zip node)))))

(defn- elt 
 ([tag] (elt tag nil))
 ([tag attrs & content]
   {:tag tag
    :attrs attrs
    :content content}))



(set-test tag=
  (are (test-step _1 _2 _3)
    true (tag= :foo) (elt :foo)
    false (tag= :bar) (elt :foo)))

(set-test id=
  (are (test-step _1 _2 _3)
    true (id= "foo") (elt :div {:id "foo"})
    false (id= "bar") (elt :div {:id "foo"})
    false (id= "foo") (elt :div)))

(set-test attr? 
  (are (test-step _1 _2 _3)
    true (attr? :href) (elt :a {:href "http://cgrand.net/"})
    false (attr? :href) (elt :a {:name "toc"})
    false (attr? :href :title) (elt :a {:href "http://cgrand.net/"})
    true (attr? :href :title) (elt :a {:href "http://cgrand.net/" :title "home"})))
    
(set-test attr= 
  (are (test-step _1 _2 (elt :a {:href "http://cgrand.net/" :title "home"}))
    true (attr= :href "http://cgrand.net/")
    false (attr= :href "http://clojure.org/")
    false (attr= :href "http://cgrand.net/" :name "home") 
    false (attr= :href "http://cgrand.net/" :title "homepage")
    true (attr= :href "http://cgrand.net/" :title "home")))
    
(set-test attr-starts
  (are (test-step _1 _2 (elt :a {:href "http://cgrand.net/" :title "home"}))
    true (attr-starts :href "http://cgr")
    false (attr-starts :href "http://clo")
    false (attr-starts :href "http://cgr" :name "ho")
    false (attr-starts :href "http://cgr" :title "x") 
    true (attr-starts :href "http://cgr" :title "ho")))

(set-test attr-ends
  (are (test-step _1 _2 (elt :a {:href "http://cgrand.net/" :title "home"}))
    true (attr-ends :href "d.net/")
    false (attr-ends :href "e.org/")
    false (attr-ends :href "d.net/" :name "me")
    false (attr-ends :href "d.net/" :title "hom")
    true (attr-ends :href "d.net/" :title "me")))

(set-test attr-contains
  (are (test-step _1 _2 (elt :a {:href "http://cgrand.net/" :title "home"}))
    true (attr-contains :href "rand")
    false (attr-contains :href "jure")
    false (attr-contains :href "rand" :name "om") 
    false (attr-contains :href "rand" :title "pa")
    true (attr-contains :href "rand" :title "om")))

(set-test nth-child
  (are (same? _2 (sniptest "<dl><dt>1<dt>2<dt>3<dt>4<dt>5" _1 (add-class "foo")))    
    [[:dt (nth-child 2)]] "<dl><dt>1<dt class=foo>2<dt>3<dt>4<dt>5" 
    [[:dt (nth-child 2 0)]] "<dl><dt>1<dt class=foo>2<dt>3<dt class=foo>4<dt>5" 
    [[:dt (nth-child 3 1)]] "<dl><dt class=foo>1<dt>2<dt>3<dt class=foo>4<dt>5" 
    [[:dt (nth-child -1 3)]] "<dl><dt class=foo>1<dt class=foo>2<dt class=foo>3<dt>4<dt>5" 
    [[:dt (nth-child 3 -1)]] "<dl><dt>1<dt class=foo>2<dt>3<dt>4<dt class=foo>5"))
      
(set-test nth-last-child
  (are (same? _2 (sniptest "<dl><dt>1<dt>2<dt>3<dt>4<dt>5" _1 (add-class "foo")))    
    [[:dt (nth-last-child 2)]] "<dl><dt>1<dt>2<dt>3<dt class=foo>4<dt>5" 
    [[:dt (nth-last-child 2 0)]] "<dl><dt>1<dt class=foo>2<dt>3<dt class=foo>4<dt>5" 
    [[:dt (nth-last-child 3 1)]] "<dl><dt>1<dt class=foo>2<dt>3<dt>4<dt class=foo>5" 
    [[:dt (nth-last-child -1 3)]] "<dl><dt>1<dt>2<dt class=foo>3<dt class=foo>4<dt class=foo>5" 
    [[:dt (nth-last-child 3 -1)]] "<dl><dt class=foo>1<dt>2<dt>3<dt class=foo>4<dt>5"))

(set-test nth-of-type
  (are (same? _2 (sniptest "<dl><dt>1<dd>def #1<dt>2<dt>3<dd>def #3<dt>4<dt>5" _1 (add-class "foo")))    
    [[:dt (nth-of-type 2)]] "<dl><dt>1<dd>def #1<dt class=foo>2<dt>3<dd>def #3<dt>4<dt>5" 
    [[:dt (nth-of-type 2 0)]] "<dl><dt>1<dd>def #1<dt class=foo>2<dt>3<dd>def #3<dt class=foo>4<dt>5" 
    [[:dt (nth-of-type 3 1)]] "<dl><dt class=foo>1<dd>def #1<dt>2<dt>3<dd>def #3<dt class=foo>4<dt>5" 
    [[:dt (nth-of-type -1 3)]] "<dl><dt class=foo>1<dd>def #1<dt class=foo>2<dt class=foo>3<dd>def #3<dt>4<dt>5" 
    [[:dt (nth-of-type 3 -1)]] "<dl><dt>1<dd>def #1<dt class=foo>2<dt>3<dd>def #3<dt>4<dt class=foo>5"))
   
(set-test nth-last-of-type
  (are (same? _2 (sniptest "<dl><dt>1<dd>def #1<dt>2<dt>3<dd>def #3<dt>4<dt>5" _1 (add-class "foo")))    
    [[:dt (nth-last-of-type 2)]] "<dl><dt>1<dd>def #1<dt>2<dt>3<dd>def #3<dt class=foo>4<dt>5" 
    [[:dt (nth-last-of-type 2 0)]] "<dl><dt>1<dd>def #1<dt class=foo>2<dt>3<dd>def #3<dt class=foo>4<dt>5" 
    [[:dt (nth-last-of-type 3 1)]] "<dl><dt>1<dd>def #1<dt class=foo>2<dt>3<dd>def #3<dt>4<dt class=foo>5" 
    [[:dt (nth-last-of-type -1 3)]] "<dl><dt>1<dd>def #1<dt>2<dt class=foo>3<dd>def #3<dt class=foo>4<dt class=foo>5" 
    [[:dt (nth-last-of-type 3 -1)]] "<dl><dt class=foo>1<dd>def #1<dt>2<dt>3<dd>def #3<dt class=foo>4<dt>5"))
    
(set-test has    
  (is-same "<div><p>XXX<p class='ok'><a>link</a><p>YYY" 
    (sniptest "<div><p>XXX<p><a>link</a><p>YYY" 
      [[:p (has [:a])]] (add-class "ok"))))

(set-test but    
  (is-same "<div><p>XXX<p><a class='ok'>link</a><p>YYY" 
    (sniptest "<div><p>XXX<p><a>link</a><p>YYY" 
      [:div (but :p)] (add-class "ok")))
      
  (is-same "<div><p class='ok'>XXX<p><a>link</a><p class='ok'>YYY" 
    (sniptest "<div><p>XXX<p><a>link</a><p>YYY" 
      [[:p (but (has [:a]))]] (add-class "ok"))))

(set-test left
  (are (same? _2 (sniptest "<div><h1>T1<h2>T2<h3>T3<p>XXX" _1 (add-class "ok"))) 
    [[:h3 (left :h2)]] "<div><h1>T1<h2>T2<h3 class=ok>T3<p>XXX" 
    [[:h3 (left :h1)]] "<div><h1>T1<h2>T2<h3>T3<p>XXX" 
    [[:h3 (left :p)]] "<div><h1>T1<h2>T2<h3>T3<p>XXX"))

(set-test lefts
  (are (same? _2 (sniptest "<div><h1>T1<h2>T2<h3>T3<p>XXX" _1 (add-class "ok"))) 
    [[:h3 (lefts :h2)]] "<div><h1>T1<h2>T2<h3 class=ok>T3<p>XXX" 
    [[:h3 (lefts :h1)]] "<div><h1>T1<h2>T2<h3 class=ok>T3<p>XXX" 
    [[:h3 (lefts :p)]] "<div><h1>T1<h2>T2<h3>T3<p>XXX")) 
      
(set-test right
  (are (same? _2 (sniptest "<div><h1>T1<h2>T2<h3>T3<p>XXX" _1 (add-class "ok"))) 
    [[:h2 (right :h3)]] "<div><h1>T1<h2 class=ok>T2<h3>T3<p>XXX" 
    [[:h2 (right :p)]] "<div><h1>T1<h2>T2<h3>T3<p>XXX" 
    [[:h2 (right :h1)]] "<div><h1>T1<h2>T2<h3>T3<p>XXX")) 

(set-test rights  
  (are (same? _2 (sniptest "<div><h1>T1<h2>T2<h3>T3<p>XXX" _1 (add-class "ok"))) 
    [[:h2 (rights :h3)]] "<div><h1>T1<h2 class=ok>T2<h3>T3<p>XXX" 
    [[:h2 (rights :p)]] "<div><h1>T1<h2 class=ok>T2<h3>T3<p>XXX" 
    [[:h2 (rights :h1)]] "<div><h1>T1<h2>T2<h3>T3<p>XXX")) 

(set-test any-node 
  (is (= 3 (-> "<html><body><i>this</i> is a <i>test</i>" html-snippet 
             (select [:body :> any-node]) count))))  

(set-test transform
  (is-same "<div>" (sniptest "<div><span>" [:span] nil))
  (is-same "<!-- comment -->" (sniptest "<!-- comment -->" [:span] nil)))
  
(set-test clone-for
  ;; node selector
  (is-same "<ul><li>one<li>two" 
    (sniptest "<ul><li>" [:li] (clone-for [x ["one" "two"]] (content x))))
  ;; fragment selector
  (is-same "<dl><dt>term #1<dd>desc #1<dt>term #2<dd>desc #2"
    (sniptest "<dl><dt>Sample term<dd>sample description" 
      {[:dt] [:dd]} (clone-for [[t d] {"term #1" "desc #1" "term #2" "desc #2"}] 
                      [:dt] (content t) 
                      [:dd] (content d))))) 

(set-test move
  (are (same? _2 
         (sniptest "<body><span>1</span><div id=target>here</div><span>2</span>" 
           (move [:span] [:div] _1) ))
    substitute "<body><span>1</span><span>2</span>"
    content "<body><div id=target><span>1</span><span>2</span></div>"
    after "<body><div id=target>here</div><span>1</span><span>2</span>"
    before "<body><span>1</span><span>2</span><div id=target>here</div>"
    append "<body><div id=target>here<span>1</span><span>2</span></div>"
    prepend "<body><div id=target><span>1</span><span>2</span>here</div>")
  (are (same? _2 
         (sniptest "<div><h1>Title1<p>blabla<hr><h2>Title2<p>blibli" 
           (move {[:h1] [:p]} {[:h2] [:p]} _1) ))
    substitute "<div><hr><h1>Title1<p>blabla"
    after "<div><hr><h2>Title2<p>blibli<h1>Title1<p>blabla"
    before "<div><hr><h1>Title1<p>blabla<h2>Title2<p>blibli")
  (are (same? _2 
         (sniptest "<div><h1>Title1<p>blabla<hr><h2>Title2<p>blibli" 
           (move {[:h1] [:p]} [:h2] _1) ))
    substitute "<div><hr><h1>Title1<p>blabla<p>blibli"
    content "<div><hr><h2><h1>Title1</h1><p>blabla</p></h2><p>blibli"
    after "<div><hr><h2>Title2<h1>Title1<p>blabla<p>blibli"
    before "<div><hr><h1>Title1<p>blabla<h2>Title2<p>blibli"
    append "<div><hr><h2>Title2<h1>Title1</h1><p>blabla</p></h2><p>blibli"
    prepend "<div><hr><h2><h1>Title1</h1><p>blabla</p>Title2</h2><p>blibli")
  (are (same? _2 
         (sniptest "<div><h1>Title1<p>blabla<hr><h2>Title2<p>blibli" 
           (move [:h1] {[:h2] [:p]} _1) ))
    substitute "<div><p>blabla<hr><h1>Title1"
    after "<div><p>blabla<hr><h2>Title2<p>blibli<h1>Title1"
    before "<div><p>blabla<hr><h1>Title1<h2>Title2<p>blibli"))

(set-test wrap
  (is-same "<dl><ol><dt>Sample term</dt></ol><dd>sample description</dd></dl>"
    (sniptest "<dl><dt>Sample term<dd>sample description" [:dt] (wrap :ol)))
  (is-same "<dl><ol><dt>Sample term</dt><dd>sample description</dd></ol></dl>"
    (sniptest "<dl><dt>Sample term<dd>sample description" {[:dt] [:dd]} (wrap :ol))))

(set-test select 
  (is (= 3 (-> "<html><body><h1>hello</h1>" html-snippet (select [:*]) count))))
  
(set-test emit*
  (is (= "<h1>hello&lt;<script>if (im < bad) document.write('&lt;')</script></h1>"
        (sniptest "<h1>hello&lt;<script>if (im < bad) document.write('&lt;')"))))

(set-test transform-content
  (is-same "<div><div class='bar'><div>"
    (sniptest "<div><div><div>" 
      [:> :div] (transform-content [:> :div] (add-class "bar")))))
      