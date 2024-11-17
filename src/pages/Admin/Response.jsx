import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Response.css';
import Header from '../../components/Header/Header';
import axios from 'axios';
import FormDetailsResponse from '../../components/Form/FormDetailsResponse';
import FormResponse from '../../components/Form/FormResponse';
import GaugeCard from '../../components/Card/GaugeCardRes';

function Response() {
    const [responses, setResponses] = useState([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPage, setTotalPage] = useState(1);
    const [limit, setLimit] = useState(8);
    const [error, setError] = useState(null);
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedType, setSelectedType] = useState('all');
    const [totalResponses, setTotalResponses] = useState(0);
    const [pendingCount, setPendingCount] = useState(0);
    const [approvedCount, setApprovedCount] = useState(0);
    const [rejectedCount, setRejectedCount] = useState(0);
    const [selectedResponse, setSelectedResponse] = useState(null);
    const [isDetailsOpen, setIsDetailsOpen] = useState(false);
    const [isResponseOpen, setIsResponseOpen] = useState(false);
    const [selectedResponseForUpdate, setSelectedResponseForUpdate] = useState(null);

    const getUserIdFromToken = (token) => {
        try {
            const payload = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payload));
            return decodedPayload.UserId;
        } catch (error) {
            console.error("Unable to decode token:", error);
            return null;
        }
    };

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    const fetchResponses = async (page, limit, status = 'all') => {
        try {
            const token = getCookie('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                return;
            }

            let url = '';
            if (status === 'all') {
                url = `http://localhost:1010/api/contact/view/all?page=${page}&limit=${limit}`;
            } else if (status === 'COMPLETED') {
                url = `http://localhost:1010/api/contact/view/all/complete?page=${page}&limit=${limit}`;
            } else if (status === 'WAITING') {
                url = `http://localhost:1010/api/contact/view/all/waiting?page=${page}&limit=${limit}`;
            }

            const response = await fetch(url, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            const data = await response.json();
            console.log('Response data:', data);

            setResponses(data.listContacts || []);
            setCurrentPage(data.currentPage);
            setTotalPage(data.totalPage);
            setLimit(data.limit);

        } catch (error) {
            console.error('Error fetching responses:', error);
            setError("Không thể lấy thông tin phản hồi.");
        }
    };

    const fetchTotalCounts = async () => {
        try {
            const token = getCookie('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                return;
            }

            const firstPageUrl = `http://localhost:1010/api/contact/view/all?page=1&limit=${limit}`;
            const firstPageResponse = await fetch(firstPageUrl, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!firstPageResponse.ok) {
                throw new Error('Network response was not ok');
            }

            const firstPageData = await firstPageResponse.json();
            const totalPages = firstPageData.totalPage;

            let allResponses = [];
            for (let page = 1; page <= totalPages; page++) {
                const url = `http://localhost:1010/api/contact/view/all?page=${page}&limit=${limit}`;
                const response = await fetch(url, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });

                if (!response.ok) {
                    throw new Error(`Failed to fetch page ${page}`);
                }

                const data = await response.json();
                allResponses = allResponses.concat(data.listContacts || []);
            }

            const waiting = allResponses.filter(r => r.status === 'WAITING').length;
            const completed = allResponses.filter(r => r.status === 'COMPLETED').length;
            const rejected = allResponses.filter(r => r.status === 'REJECTED').length;
            const total = allResponses.length;

            setPendingCount(waiting);
            setApprovedCount(completed);
            setRejectedCount(rejected);
            setTotalResponses(total);

        } catch (error) {
            console.error('Error fetching total counts:', error);
            setError("Không thể lấy thông tin tổng số lượng phản hồi.");
        }
    };

    useEffect(() => {
        fetchResponses(currentPage, limit, selectedType);
        fetchTotalCounts();
    }, [currentPage, limit, selectedType]);

    // Tính toán phần trăm cho từng trạng thái
    const pendingPercentage = totalResponses ? ((pendingCount / totalResponses) * 100).toFixed(0) : 0;
    const approvedPercentage = totalResponses ? ((approvedCount / totalResponses) * 100).toFixed(0) : 0;
    const rejectedPercentage = totalResponses ? ((rejectedCount / totalResponses) * 100).toFixed(0) : 0;

    // Lọc responses dựa trên searchTerm và selectedType
    const filteredResponses = responses.filter(response => {
        const matchesSearch =
            (response.description?.toLowerCase() || '').includes(searchTerm.toLowerCase()) ||
            (response.userId?.toString() || '').includes(searchTerm);
        const matchesType = selectedType === 'all' || response.status === selectedType;
        return matchesSearch && matchesType;
    });

    useEffect(() => {
        console.log("Current responses:", responses);
        console.log("Filtered responses:", filteredResponses);
    }, [responses, filteredResponses]);

    const handleTypeChange = (event) => {
        const selectedStatus = event.target.value;
        setSelectedType(selectedStatus);
        fetchResponses(1, limit, selectedStatus);
    };

    const handleSearchChange = (event) => {
        setSearchTerm(event.target.value);
    };

    const handlePageChange = (newPage) => {
        console.log(`Attempting to change to page: ${newPage}`);
        if (newPage > 0 && newPage <= totalPage) {
            setCurrentPage(newPage);
        }
    };

    const getPaginationNumbers = () => {
        const paginationNumbers = [];
        const maxButtons = 5;

        if (totalPage <= maxButtons) {
            for (let i = 1; i <= totalPage; i++) {
                paginationNumbers.push(i);
            }
        } else {
            paginationNumbers.push(1);

            if (currentPage > 3) {
                paginationNumbers.push('...');
            }

            const startPage = Math.max(2, currentPage - 1);
            const endPage = Math.min(totalPage - 1, currentPage + 1);

            for (let i = startPage; i <= endPage; i++) {
                paginationNumbers.push(i);
            }

            if (currentPage < totalPage - 2) {
                paginationNumbers.push('...');
            }

            paginationNumbers.push(totalPage);
        }

        return paginationNumbers;
    };

    const handleDetailsClick = (response) => {
        setSelectedResponse(response);
        setIsDetailsOpen(true);
    };

    const handleCheckboxChange = (response) => {
        console.log(`Checkbox for response ${response.contactId} is now ${!response.isChecked}`);
        // Cập nhật state hoặc thực hiện hành động khác ở đây
    };

    const handleUpdateClick = (response) => {
        setSelectedResponseForUpdate(response);
        setIsResponseOpen(true);
    };

    return (
        <div className="response-table">
            <Header isMenuOpen={isMenuOpen} toggleMenu={() => setIsMenuOpen(!isMenuOpen)} title="Phản Hồi" />
            {isDetailsOpen && (
                <div className="overlay-details-response">
                    <FormDetailsResponse
                        response={selectedResponse}
                        onClose={() => setIsDetailsOpen(false)}
                    />
                </div>
            )}
            {isResponseOpen && (
                <div className="overlay-response">
                    <FormResponse
                        response={selectedResponseForUpdate}
                        onClose={() => {
                            setIsResponseOpen(false);
                            fetchResponses(currentPage, limit, selectedType);
                        }}
                    />
                </div>
            )}
            <div className={`response-table-row ${isMenuOpen ? 'response-dimmed' : ''}`}>
                <div className="response-main-section">
                    <div className="response-box">
                        <div className="header-response-box">
                            <h2>Danh Sách Phản Hồi</h2>
                            <input
                                type="text"
                                placeholder="Tìm kiếm phản hồi..."
                                className="search-response-input"
                                onChange={handleSearchChange}
                                id="search-response"
                            />
                            <select
                                value={selectedType}
                                onChange={handleTypeChange}
                                className="type-select"
                                style={{ width: '40%', borderRadius: '20px' }}
                            >
                                <option value="all">Tất cả</option>
                                <option value="COMPLETED">Đã duyệt</option>
                                <option value="WAITING">Đang chờ</option>
                            </select>
                        </div>

                        {error && <div className="error-message">{error}</div>}

                        <table>
                            <thead>
                                <tr>
                                    <th>STT</th>
                                    <th>Mã người dùng</th>
                                    <th>Nội dung</th>
                                    <th>Trạng thái</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredResponses.length > 0 ? filteredResponses.map((response, index) => (
                                    <tr key={response.contactId}>
                                        <td>{(currentPage - 1) * limit + index + 1}</td>
                                        <td>{response.userId}</td>
                                        <td>{response.description}</td>
                                        <td>
                                            <span className={`status-${response.status.toLowerCase()}`}>
                                                {response.status === 'WAITING' ? 'Đang chờ' :
                                                    response.status === 'COMPLETED' ? 'Đã duyệt' : 'Từ chối'}
                                            </span>
                                        </td>
                                        <td>
                                            <div className="response-button-container">
                                                <button id="response-update-btn3" onClick={() => handleDetailsClick(response)}>
                                                    <i className="ti-info-alt"></i>
                                                </button>
                                                <button id="response-update-btn1" onClick={() => handleUpdateClick(response)}>
                                                    <i className="ti-pencil"></i>
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                )) : (
                                    <tr>
                                        <td colSpan="5" style={{ textAlign: 'center' }}>Không có kết quả tìm kiếm.</td>
                                    </tr>
                                )}
                            </tbody>
                        </table>

                        <div className="response-admin-pagination">
                            <button
                                className="response-btn btn-pre"
                                onClick={() => handlePageChange(currentPage - 1)}
                                disabled={currentPage === 1}
                            >
                                &lt;
                            </button>
                            {getPaginationNumbers().map((number, index) => (
                                <button
                                    key={index}
                                    className={`response-btn ${number === currentPage ? 'response-btn-page' : 'response-btn-light'}`}
                                    onClick={() => typeof number === 'number' && handlePageChange(number)}
                                    disabled={typeof number !== 'number'}
                                >
                                    {number}
                                </button>
                            ))}
                            <button
                                className="response-btn btn-next"
                                onClick={() => handlePageChange(currentPage + 1)}
                                disabled={currentPage === totalPage}
                            >
                                &gt;
                            </button>
                        </div>
                    </div>

                    <div className="response-stats-section">
                        <GaugeCard
                            percentage={pendingPercentage}
                            width='300px'
                            height='150px'
                            data="Đang Chờ"
                            number1={pendingCount}
                            description="phản hồi"
                            color='#ffffff'
                            backgroundColor='#F9C5CA'
                        />
                        <GaugeCard
                            percentage={approvedPercentage}
                            width='300px'
                            height='150px'
                            data="Đã duyệt"
                            number1={approvedCount}
                            description="phản hồi"
                            color='#ffffff'
                            backgroundColor='#B0D1D8'
                        />
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Response;
