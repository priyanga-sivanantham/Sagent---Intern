import React, { useState, useEffect, useMemo } from 'react';
import { Search, SlidersHorizontal, X } from 'lucide-react';
import { getAllProducts } from '../api/api';
import ProductCard from '../components/ProductCard';
import { useAuth } from '../context/AuthContext';
import './Dashboard.css';
import Chatbot from "../components/chatbot/Chatbot";
const CATEGORIES = ['All','Fruits','Vegetables','Dairy','Bakery','Meat','Beverages','Snacks','Frozen','Personal','Household'];

const BANNERS = [
  { emoji: '🥦', title: 'Fresh Veggies', sub: 'Farm to doorstep daily', bg: '#E8F5E9', accent: '#0C831F' },
  { emoji: '🍎', title: 'Seasonal Fruits', sub: 'Handpicked specials', bg: '#FFF3E0', accent: '#E65100' },
  { emoji: '⚡', title: '10-Min Delivery', sub: 'Order now!', bg: '#EDE7F6', accent: '#5E35B1' },
];

export default function Dashboard() {
  const { user } = useAuth();
  const [products, setProducts]   = useState([]);
  const [loading, setLoading]     = useState(true);
  const [search, setSearch]       = useState('');
  const [category, setCategory]   = useState('All');
  const [sortBy, setSortBy]       = useState('default');
  const [bannerIdx, setBannerIdx] = useState(0);

  useEffect(() => {
    const iv = setInterval(() => setBannerIdx(i => (i + 1) % BANNERS.length), 3500);
    return () => clearInterval(iv);
  }, []);

  useEffect(() => {
    getAllProducts()
      .then(r => setProducts(r.data || []))
      .catch(() => setProducts([]))
      .finally(() => setLoading(false));
  }, []);

  const filtered = useMemo(() => {
    let p = [...products];
    if (category !== 'All') p = p.filter(x => x.productCategory?.toLowerCase() === category.toLowerCase());
    if (search) p = p.filter(x => x.productName?.toLowerCase().includes(search.toLowerCase()));
    if (sortBy === 'price-asc') p.sort((a, b) => a.productPrice - b.productPrice);
    if (sortBy === 'price-desc') p.sort((a, b) => b.productPrice - a.productPrice);
    if (sortBy === 'name') p.sort((a, b) => a.productName?.localeCompare(b.productName));
    return p;
  }, [products, category, search, sortBy]);

  const banner = BANNERS[bannerIdx];

  return (
    <div className="dashboard">
      {/* Hero banner */}
      <div className="hero-banner" style={{ background: banner.bg }}>
        <div className="hero-content">
          <span className="hero-emoji">{banner.emoji}</span>
          <div>
            <h2 style={{ color: banner.accent }}>{banner.title}</h2>
            <p>{banner.sub}</p>
          </div>
        </div>
        <div className="banner-dots">
          {BANNERS.map((_, i) => (
            <button
              key={i}
              className={`dot ${i === bannerIdx ? 'active' : ''}`}
              onClick={() => setBannerIdx(i)}
              style={{ '--accent': banner.accent }}
            />
          ))}
        </div>
      </div>

      {/* Search + sort */}
      <div className="toolbar">
        <div className="search-box">
          <Search size={18} />
          <input
            type="text"
            placeholder="Search products..."
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
          {search && <button onClick={() => setSearch('')}><X size={16} /></button>}
        </div>

        <div className="sort-box">
          <SlidersHorizontal size={16} />
          <select value={sortBy} onChange={e => setSortBy(e.target.value)}>
            <option value="default">Sort by</option>
            <option value="price-asc">Price: Low → High</option>
            <option value="price-desc">Price: High → Low</option>
            <option value="name">Name A–Z</option>
          </select>
        </div>
      </div>

      {/* Category pills */}
      <div className="category-scroll">
        {CATEGORIES.map(c => (
          <button
            key={c}
            className={`category-tag ${category === c ? 'active' : ''}`}
            onClick={() => setCategory(c)}
          >
            {c}
          </button>
        ))}
      </div>

      {/* Section header */}
      <div className="section-header">
        <h3>
          {category === 'All' ? 'All Products' : category}
          {!loading && <span className="count">{filtered.length}</span>}
        </h3>
      </div>

      {/* Grid */}
      {loading ? (
        <div className="products-grid">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className="skeleton-card">
              <div className="skeleton" style={{ height: 130 }} />
              <div style={{ padding: 14, display: 'flex', flexDirection: 'column', gap: 8 }}>
                <div className="skeleton" style={{ height: 12, width: '60%' }} />
                <div className="skeleton" style={{ height: 16, width: '90%' }} />
                <div className="skeleton" style={{ height: 14, width: '40%' }} />
              </div>
            </div>
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <div className="empty-state">
          <span>🔍</span>
          <h3>No products found</h3>
          <p>Try a different search or category</p>
          <button className="btn btn-outline" onClick={() => { setSearch(''); setCategory('All'); }}>
            Clear filters
          </button>
        </div>
      ) : (
        <div className="products-grid">
          {filtered.map(p => <ProductCard key={p.productId} product={p} />)}
        </div>
      )}
      <Chatbot />
    </div>
  );
}
